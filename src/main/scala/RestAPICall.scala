import java.io.{BufferedOutputStream, File, FileInputStream, FileOutputStream}
import java.net.URL
import java.nio.charset.StandardCharsets
import java.util.Base64

import javax.net.ssl.HttpsURLConnection


object RestAPICall {
   val log = org.apache.log4j.LogManager.getLogger(getClass().getName())
    
   def getRestAPIData(url1: String, user: String, pass : String, outputDir :String) : Unit = {
    
     var authString = user + ":" + pass
     print("authString : " +authString)
     var authEncBytes = authString.getBytes(StandardCharsets.UTF_8)
     val decodedAuth = Base64.getEncoder.encode(authEncBytes)
     ///changes added today
     var uri = url1
     val obj:URL = new java.net.URL(uri)
     val connection:HttpsURLConnection = obj.openConnection().asInstanceOf[HttpsURLConnection]
     connection.setRequestMethod("GET")
     connection.setDoOutput(true)
     connection.setRequestProperty("Authorization","Basic " +decodedAuth)
     var file = new File(outputDir)  
     var inputdata = connection.getInputStream
     var outputdata = new BufferedOutputStream(new FileOutputStream(file))
     var b = inputdata.read()
     while(b != -1) {
        outputdata.write(b)
        b = inputdata.read()
     }
     outputdata.close()
     inputdata.close()
   }
   
   def main(args: Array[String]) {
    if (args.length != 3) {
      System.out.println("3 arguments required:  output path of csv, logfiledir, logprop")
      System.exit(-1)
    }
    try {
      var outputDir = ""
      
      outputDir = args(0)
      val logfile = args(1)
      val logprops = new java.util.Properties()
      val input = new FileInputStream(args(2))
      logprops.load(input)
      val secondsdateformat = new java.text.SimpleDateFormat("yyyyMMddHHmmss")
      val curDate = new java.util.Date
      logprops.setProperty("log4j.appender.FILE.File", logfile + "_" + secondsdateformat.format(curDate) + ".log")
      org.apache.log4j.LogManager.resetConfiguration()
      org.apache.log4j.PropertyConfigurator.configure(logprops)

      //read for prop reader

      var customProp = new PropReader()
      var props = customProp.propReader(s"Fields.properties")
      var usrName = props.getProperty("usrName")
      var passwrd = props.getProperty("passwrd")
      var accessUrl = props.getProperty("Url")
      var data = getRestAPIData(accessUrl,usrName,passwrd,outputDir)

    }  
    catch {
      case e: Exception =>
        e.printStackTrace()
        log.info("Rest API access failed")
        throw e
    }
  }
}   