package controllers

import javax.inject.Inject

import models.ProjectRepo
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.Json
import play.api.mvc.{Action, Controller}

class Application @Inject()(projectRepo: ProjectRepo)
                           extends Controller {

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
