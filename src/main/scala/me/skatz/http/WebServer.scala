package me.skatz.http

import scala.language.postfixOps
import me.skatz.utils.Configuration
import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer
import java.io.{File, FileInputStream, IOException}
import java.net.InetSocketAddress

object WebServer {
  @throws[IOException]
  def main(args: Array[String]): Unit = {
    val server = HttpServer.create(new InetSocketAddress(Configuration.serverPort.toInt), 0)
    val context = server.createContext("/")
    context.setHandler(WebServer.handleRequest)
    server.start()
  }

  @throws[IOException]
  private def handleRequest(exchange: HttpExchange): Unit = {
    val root = "./src/main/resources/http/"
    val uri = exchange.getRequestURI
    System.out.println("looking for: " + root + uri.getPath)
    val path = uri.getPath
    val file = new File(root + path).getCanonicalFile

    if (!file.isFile) { // Object does not exist or is not a file: reject with 404 error.
      val response = "404 (Not Found)\n"
      exchange.sendResponseHeaders(404, response.length)
      val os = exchange.getResponseBody
      os.write(response.getBytes)
      os.close()
    }
    else { // Object exists and is a file: accept with response code 200.
      var mime = "text/html"
      if (path.substring(path.length - 3) == ".js") mime = "application/javascript"
      if (path.substring(path.length - 3) == "css") mime = "text/css"
      val h = exchange.getResponseHeaders
      h.set("Content-Type", mime)
      exchange.sendResponseHeaders(200, 0)
      val os = exchange.getResponseBody
      val fs = new FileInputStream(file)
      val buffer = new Array[Byte](0x10000)
      var count = 0

      do {
        count = fs.read(buffer)
        os.write(buffer, 0, count)
      } while (count >= 0)

      fs.close()
      os.close()
    }
  }
}