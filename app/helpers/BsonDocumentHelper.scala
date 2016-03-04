package helpers

import com.github.jeroenr.bson.BsonDocument
import play.api.libs.json.{Writes, Json, JsValue}

object BsonDocumentHelper {

  implicit val bsonWrites = new Writes[BsonDocument]{
    override def writes(o: BsonDocument): JsValue = Json.parse(o.toJson())
  }

  implicit def bsonToJson(bson: BsonDocument): JsValue = Json.toJson[BsonDocument](bson)
}
