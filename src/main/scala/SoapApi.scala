

import java.io._
import java.net.URL
import java.nio.charset.Charset

import javax.net.ssl.HttpsURLConnection
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.{OutputKeys, TransformerFactory}
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult
import org.w3c.dom.Document
import org.xml.sax.InputSource;

object SoapApi {
  
    val log = org.apache.log4j.LogManager.getLogger(getClass().getName())
    
  def getSOAPAPIData(url1: String, user: String, pass : String, outputDir :String) : Unit = {
		// Code to make a webservice HTTP request
		var responseString = ""
		var outputString = ""
		var wsEndPoint = url1
		val obj:URL = new java.net.URL(wsEndPoint)
		val connection:HttpsURLConnection = obj.openConnection().asInstanceOf[HttpsURLConnection]
		
		var bout : ByteArrayOutputStream = new ByteArrayOutputStream();
		var xmlInput : String = 
		  "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:web=\"http://webservice.services.ads.com/\"><soapenv:Header><web:Password>Analytix2020</web:Password><web:Username>public</web:Username></soapenv:Header><soapenv:Body><web:getMappingById><outputFormat>xlsx</outputFormat><mappingId>15613</mappingId></web:getMappingById></soapenv:Body></soapenv:Envelope>"
		
		//var buffer = new bytes(xmlInput.length())
	  var buffer = xmlInput.getBytes();
		bout.write(buffer);
		var b = bout.toByteArray()
		var SOAPAction: String = "getMappingById"
		connection.setRequestProperty("Content-Length", String.valueOf(b.length));
		connection.setRequestProperty("Content-Type", "text/xml; charset=utf-8");
		connection.setRequestProperty("SOAPAction", SOAPAction);
		connection.setRequestMethod("POST");
		connection.setDoOutput(true);
		connection.setDoInput(true);
		var out = connection.getOutputStream();
		// Write the content of the request to the outputstream of the HTTP
		// Connection.
		out.write(b);
		out.close();
		// Ready with sending the request.
		// Read the response.
		var isr = new InputStreamReader(connection.getInputStream(), Charset.forName("UTF-8"));
		var in = new BufferedReader(isr);
		// Write the SOAP message response to a String.
		while ((responseString = in.readLine()) != null) {
						outputString = outputString + responseString;
		}
		// Write the SOAP message formatted to the console.
		var formattedSOAPResponse = formatXML(outputString);
		System.out.println(formattedSOAPResponse);
	}
    
  def formatXML(unformattedXml : String) : String = 
  {
		try {
			var document = parseXmlFile(unformattedXml);
			var transformerFactory = TransformerFactory.newInstance();
			transformerFactory.setAttribute("getMappingById", 3);
			var transformer = transformerFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			var source = new DOMSource(document);
			var xmlOutput = new StreamResult(new StringWriter());
			transformer.transform(source, xmlOutput);
			return xmlOutput.getWriter().toString();
		} 
		catch {
		case e: Exception =>
        e.printStackTrace()
        log.info("Soap API parsing failed")
        throw new RuntimeException(e)
		}
	}  
  
  def parseXmlFile(in : String) : Document =
  {
		try {
			var dbf = DocumentBuilderFactory.newInstance();
			var db = dbf.newDocumentBuilder();
			var is = new InputSource(new StringReader(in));
			return db.parse(is)
		} 
		catch {
		    case e: Exception =>
        e.printStackTrace()
        log.info("Soap API parsing failed")
        throw new RuntimeException(e)
		}
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
      var data = getSOAPAPIData(accessUrl,usrName,passwrd,outputDir)
    }  
    catch {
      case e: Exception =>
        e.printStackTrace()
        log.info("Soap API access failed")
        throw e
    }
  }
	
}