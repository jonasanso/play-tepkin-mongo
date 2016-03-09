package controllers

import javax.inject.Inject

import akka.actor.ActorRef
import akka.stream.scaladsl.{Source, Flow}
import akka.util.ByteString
import models.{Project, ProjectRepo}
import play.api.http.HttpEntity.Streamed
import play.api.http.MimeTypes
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.{JsSuccess, Json}
import play.api.mvc._
import concurrent.duration._

class Application @Inject()(projectRepo: ProjectRepo)
                           extends Controller {

  implicit val mt = play.api.mvc.WebSocket.MessageFlowTransformer.stringMessageFlowTransformer

  def socket = WebSocket.accept[String, String] { request =>
    Flow[String]
      .map(Json.parse)
      .map(js => Json.fromJson[Project](js))
      .collect { case JsSuccess(project, _) => project }
      .groupedWithin(10, 10 millis)
      .map(_.toList)
      .mapAsyncUnordered(parallelism = 4)(projectRepo.insert)
      .map(_.n.toString)
  }

  def createProject(name: String)= Action.async { implicit rs =>
    projectRepo.create(name)
      .map(id => Ok(s"project $id created") )
  }

  def listProjects = Action { implicit rs =>
    val projects = projectRepo.all
                              .map(p => Json.toJson[List[Project]](p))
                              .map(js => ByteString(js.toString()))

    Ok.sendEntity(Streamed(projects, None, Some(MimeTypes.JSON)))
  }

  def projects(name: String) = Action.async { implicit rs =>
    for {
      Some(project) <-  projectRepo.findByName(name)
    } yield Ok(Json.toJson(project))
  }

}
