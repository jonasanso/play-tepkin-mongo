package controllers

import javax.inject.Inject

import akka.stream.scaladsl.Flow
import akka.util.ByteString
import models.{Project, ProjectRepo}
import play.api.http.HttpEntity.Streamed
import play.api.http.MimeTypes
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.{JsSuccess, Json}
import play.api.mvc.WebSocket.MessageFlowTransformer
import play.api.mvc._

import scala.concurrent.duration._

class Application @Inject()(projectRepo: ProjectRepo) extends Controller {

  implicit val mt: MessageFlowTransformer[Project, String] = {
    MessageFlowTransformer.stringMessageFlowTransformer.map { s =>
      Json.fromJson[Project](Json.parse(s)) match {
        case JsSuccess(project, _) => project
      }
    }
  }


  def socket = WebSocket.accept[Project, String] { request =>
    Flow[Project]
      .groupedWithin(10, 10 millis)
      .map(_.toList)
      .mapAsyncUnordered(parallelism = 4)(projectRepo.insert)
      .map(_.n.toString)
  }

  def createProject(name: String) = Action.async {
    projectRepo.create(name)
      .map(id => Ok(s"project $id created"))
  }

  def listProjects = Action {
    val projects = projectRepo.all
      .map(p => Json.toJson[List[Project]](p))
      .map(js => ByteString(js.toString()))

    Ok.sendEntity(Streamed(projects, None, Some(MimeTypes.JSON)))
  }

  def projects(name: String) = Action.async {
    for {
      Some(project) <- projectRepo.findByName(name)
    } yield Ok(Json.toJson(project))
  }

}
