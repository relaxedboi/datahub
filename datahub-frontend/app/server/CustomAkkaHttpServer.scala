package server

import play.api.Logger
import play.core.server.PekkoHttpServer
import play.core.server.ServerProvider
import org.apache.pekko.http.scaladsl.settings.ParserSettings

/** Custom Pekko HTTP server that allows us to overrides some Pekko server settings as the current Play / Pekko
 *  versions we're using don't allow us to override these via conf files
 */
class CustomPekkoHttpServer(context: PekkoHttpServer.Context) extends PekkoHttpServer(context) {

  override protected def createParserSettings(): ParserSettings = {
    val defaultSettings: ParserSettings = super.createParserSettings()
    val maxHeaderCountKey = "play.http.server.pekko.max-header-count"
    if (context.config.configuration.has(maxHeaderCountKey)) {
      val maxHeaderCount = context.config.configuration.get[Int](maxHeaderCountKey)
      val logger = Logger(classOf[CustomPekkoHttpServer])
      logger.info(s"Setting max header count to: $maxHeaderCount")
      defaultSettings.withMaxHeaderCount(maxHeaderCount)
    } else
      defaultSettings
  }
}

/** A factory that instantiates a CustomPekkoHttpServerProvider. */
class CustomPekkoHttpServerProvider extends ServerProvider {
  def createServer(context: ServerProvider.Context) = {
    val serverContext = PekkoHttpServer.Context.fromServerProviderContext(context)
    new CustomPekkoHttpServer(serverContext)
  }
}
