import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

object Schema {
    fun create() {
        transaction {
            SchemaUtils.create(Users, Statistics)
        }
    }
}
