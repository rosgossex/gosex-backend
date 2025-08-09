package gosex.backend.resources

import io.ktor.resources.*
import kotlinx.serialization.Serializable

@Serializable
@Resource("/users")
class UsersResource {
  @Serializable
  @Resource("/search")
  class Search(val parent: UsersResource = UsersResource(), val q: String)

  @Serializable @Resource("/me") class Me(val parent: UsersResource = UsersResource())
}
