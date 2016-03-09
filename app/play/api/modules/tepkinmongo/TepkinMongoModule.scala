package play.api.modules.tepkinmongo

import javax.inject._

import akka.actor.ActorSystem
import com.github.jeroenr.tepkin.MongoClient
import play.api.ApplicationLoader.Context
import play.api._
import play.api.inject.{ApplicationLifecycle, Binding, Module}

import scala.concurrent.ExecutionContext

/**
  * MongoDB module.
  */
@Singleton
final class TepkinMongoModule extends Module {
  override def bindings(environment: Environment, configuration: Configuration): Seq[Binding[_]] = Seq(bind[TepkinMongoApi].to[DefaultTepkinMongoApi].in[Singleton])

}

/**
  * Cake pattern components.
  */
trait TepkinMongoComponents {
  def tepkinMongoApi: TepkinMongoApi
}


trait TepkinMongoApi {
  def client: MongoClient
}

final class DefaultTepkinMongoApi @Inject()(configuration: Configuration,
                                             applicationLifecycle: ApplicationLifecycle) extends TepkinMongoApi {

  val client = MongoClient(configuration.getString("mongodb.uri").getOrElse(throw new IllegalStateException("Please configure mongodb.uri in your application.conf for example \"mongodb://localhost\" ")))

}