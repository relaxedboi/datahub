package server

import play.api.Logger
import play.core.server.PekkoHttpServer
import play.core.server.ServerProvider
import org.apache.pekko.http.scaladsl.settings.ParserSettings

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

class CustomPekkoHttpServerProvider extends ServerProvider {
  def createServer(context: ServerProvider.Context) = {
    val serverContext = PekkoHttpServer.Context.fromServerProviderContext(context)
    new CustomPekkoHttpServer(serverContext)
  }
}
