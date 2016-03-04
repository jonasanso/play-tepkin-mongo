package controllers

import javax.inject.Inject

import akka.stream.scaladsl.Flow
import models.{Project, ProjectRepo}
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
    Ok.chunked(projectRepo.all.map(x => Json.toJson(x)))
  }

  def projects(name: String) = Action.async { implicit rs =>
    for {
      Some(project) <-  projectRepo.findByName(name)
    } yield Ok(Json.toJson(project))
  }

}
