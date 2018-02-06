import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Set;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.log4j.Logger;
import org.apache.log4j.MDC;

public class VastServlet extends HttpServlet {

   private final Logger log = Logger.getLogger(IGINXMain.class);


   protected void close(HttpServletRequest req) {
      HttpSession session = req.getSession(false);
      if(session != null) {
         synchronized(session) {
            session.invalidate();
         }
      }

   }

   protected String[] getResp(HttpServletRequest req) {
      String filename = null;
      String defaultFile = Common.propLoad().getProperty("DefaultRespFile");
      String defaultRespCode = Common.propLoad().getProperty("DefaultRespCode");
      String defaultLocation = Common.propLoad().getProperty("DefaultLocation");
      String[] resp = new String[3];
      String[] defaultResp = new String[]{defaultRespCode, defaultFile, "0"};
      ArrayList<String> urls = new ArrayList<String>();
      Set<String> allNames = Common.propLoad().stringPropertyNames();
      if(defaultFile != null && defaultRespCode != null && defaultLocation != null) {
         Iterator<String> iter = allNames.iterator();

         String temp;
         while(iter.hasNext()) {
            temp = (String)iter.next();
            if(temp.substring(0, 3).equals("url")) {
               urls.add(temp);
            }
         }

         iter = urls.iterator();

         boolean cond;
         do {
            if(!iter.hasNext()) {
               return defaultResp;
            }

            temp = (String)iter.next();
            if(req.getQueryString() != null) {
               cond = req.getRequestURI().contains(Common.propLoad().getProperty(temp)) || req.getQueryString().contains(Common.propLoad().getProperty(temp));
            } else {
               cond = req.getRequestURI().contains(Common.propLoad().getProperty(temp));
            }
         } while(!cond);

         String respNumber = temp.substring(3);
         String respCode = Common.propLoad().getProperty("code" + respNumber);
         this.log.info("There is resp code for this URL in config file: " + respCode + " respNumber is " + respNumber);
         if(respCode == null) {
            respCode = "200";
         }

         switch(respCode) {
         case "200":
            if(respCode.equals("200")) {
               String[] allResps =  Common.propLoad().getProperty("resp" + respNumber).split(",");
            	filename = allResps[(int)(Math.random()*allResps.length)];
               if(filename == null) {
                  return defaultResp;
               }

               resp[0] = "200";
               resp[1] = filename;
               resp[2] = "0";
               return resp;
            }
            break;
         case "204":
            if(respCode.equals("204")) {
               resp[0] = "204";
               resp[1] = "0";
               resp[2] = "0";
               return resp;
            }
            break;
         case "302":
            if(respCode.equals("302")) {
               String respLocation = Common.propLoad().getProperty("location" + respNumber);
               resp[0] = "302";
               resp[1] = "0";
               if(respLocation == null) {
                  resp[2] = defaultLocation;
               } else {
                  resp[2] = respLocation;
               }

               return resp;
            }
         }

         resp[0] = respCode;
         resp[1] = "0";
         resp[2] = "0";
         return resp;
      } else {
         throw new NullPointerException("You should add DefaultRespFile, DefaultRespCode and DefaultLocation to config file");
      }
   }

   protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
      String requestId = String.valueOf(Math.round(Math.random() * 1.0E9D));
      MDC.put("requestId", requestId);
      this.log.info("URL is:  " + req.getScheme() + ":/" + req.getRequestURI() + "?" + req.getQueryString());
      Enumeration<String> headers = req.getHeaderNames();

      while(headers.hasMoreElements()) {
         String out = (String)headers.nextElement();
         this.log.debug(out + ": " + req.getHeader(out));
      }

      PrintWriter out1 = resp.getWriter();
      String[] respArray = this.getResp(req);
      this.log.info("resp code " + respArray[0]);
      resp.setHeader("Set-Cookie", "luid1=w:dfhvlb:w:final:a; expires=Fri, 02-Mar-2018 10:00:00 GMT; path=/; domain=" + Common.propLoad().getProperty("cookie_domain"));
      if(req.getHeader("Origin") != null) {
         resp.setHeader("Access-Control-Allow-Origin", req.getHeader("Origin"));
         resp.setHeader("Access-Control-Allow-Credentials", "true");
      }

    
         String responce=respArray[0];
         switch(responce) 
         {
         case "200":
            if(responce.equals("200")) {
               resp.setStatus(200);
               resp.setContentType("text/xml");
               String file = respArray[1];
               FileInputStream fis = new FileInputStream(new File("html//" + file));
               Scanner in = new Scanner(fis);

               String line;
               for(long rnd = Math.round(Math.random() * 1.0E9D); in.hasNextLine(); out1.println(line)) {
                  line = in.nextLine();
                  if(line.contains("%session_id%")) {
                     line = line.replace("%session_id%", String.valueOf(rnd));
                  }
               }

               in.close();
               fis.close();
               break;
            }
            break;
         case "204":
            if(responce.equals("204")) {
               resp.setStatus(204);
               break;
            }
            break;
         case "302":
            if(responce.equals("302")) {
               resp.setStatus(302);
               resp.setHeader("Location", respArray[2]);
               break;
            }
         default:
        	 resp.setStatus(200);
             resp.setContentType("text/html");
             out1.println("<h1>OOOOPsOOOPsss...</h1>");
        	 
         }

         

      this.close(req);
   }
}
