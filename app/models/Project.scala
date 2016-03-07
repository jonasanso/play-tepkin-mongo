package models

import javax.inject.{Singleton, Inject}

import akka.actor.ActorRef
import akka.stream.scaladsl.Source
import akka.util.Timeout
import com.github.jeroenr.bson.BsonDocument
import com.github.jeroenr.bson.BsonDsl._
import com.github.jeroenr.tepkin.MongoClient
import com.typesafe.config.Config
import helpers.BsonDocumentHelper._
import play.api.{Application, Configuration}
import play.api.libs.json.{JsResult, JsSuccess, Json}

import scala.concurrent.Future
import scala.concurrent.duration._

case class Project(_id: String, name: String)

object Project {
  implicit val projectFormatter = Json.format[Project]
  def apply(bson: BsonDocument): JsResult[Project] = {
    Json.fromJson[Project](bson)
  }

  def toBsonDocument(project: Project): BsonDocument =
    ("_id" := project._id) ~
      ("name" := project.name)
}

@Singleton
class MongoConfig @Inject()(conf: Configuration) {
  val client = MongoClient(conf.getString("mongo.url").getOrElse(throw new IllegalStateException("Please configure mongo.url in your application.conf for example \"mongodb://localhost\" ")))
}

class ProjectRepo @Inject() (mongoConfig: MongoConfig) {
  implicit val timeout: Timeout = 5.seconds
  import mongoConfig.client.ec

  val projects = mongoConfig.client("tepkin")("projects")

  def create(name: String): Future[Boolean] = {
    val project = "name" := name
    projects.insert(project).map(_.ok)
  }

  def all: Source[List[Project], ActorRef] = {
    projects.find(new BsonDocument()).map(l => l.map(Project(_)).collect {
      case JsSuccess(p, _ ) => p
    })
  }

  def insert(ps: List[Project]) =
    projects.insert(ps.map(Project.toBsonDocument))

  def findByName(name: String): Future[Option[Project]] = {
    val byId = "name" := name
    projects.findOne(byId).map(_.flatMap(Project(_).asOpt))
  }


}
