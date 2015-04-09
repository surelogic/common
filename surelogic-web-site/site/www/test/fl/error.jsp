<%@ page import="com.paypal.sdk.services.*" %>
<%@ page import="com.paypal.sdk.services.NVPCallerServices" %>
<%@ page import="com.paypal.sdk.util.*" %>
<%@ page import="com.paypal.sdk.core.nvp.NVPDecoder" %>
<%@ page import="java.util.*" %>
<%@ page language="java" %>
<html>
  <head>
    <title>Checkout Error</title>
    <meta name="title" content="Error During Checkout" />
    <meta name="keywords" content="advanced,analysis,applications,assurance,assure,bug-finding,capabilities,complex,consulting,critical,defects,deliver,design,developers,development,develops,empower,engineering,intent,issues,language,novel,powerful,prevention,provides,providing,quality,reliable,services,software,solutions,SureLogic,teams,technology,tools,troublesome,verification,works" />
    <meta name="robots" content="noindex, nofollow" />
    <link rel="shortcut icon" href="images/favicon.ico" />
    <meta name="author" content="Nathan Boy" />
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    
    <link rel="shortcut icon" href="favicon.ico" type="image/x-icon" />
    <link rel="stylesheet" type="text/css" media="screen, projection" href="../../templates/surelogic_rev3/css/template_css.css" />
    
  </head>
  <body>
    <div id="container">
      <!-- top border --> 
      <div class="border_top">
	<div class="corner_left"></div>
	<div class="corner_right"></div>
      </div>
      <!-- content page band -->
      <div class="border_left">
	<div class="border_right">
	  <!-- page content starts here -->
	  <div id="content">
	    <center>
	      <br>
	      <font size=2 color=black face=Verdana><b>PayPal API Error</b></font>
	      <br><br>
	      <b>A PayPal API has returned an error!</b><br><br>
	      <table width="700">
		<%
		   NVPDecoder decoder = (NVPDecoder) session.getValue("response");
		   %>
		<tr>
		  <td>Ack:</td>
		  <td><%= decoder.get("ACK") %></td>
		</tr>
		<tr>
		  <td>Correlation ID:</td>
		  <td><%= decoder.get("CORRELATIONID") %></td>
		</tr>
		<tr>
		  <td>Version:</td>
		  <td><%= decoder.get("VERSION") %></td>
		</tr> 
		<%
		   String strErrorCode = "";
		   String strShortMessage = "";
		   String strLongMessage = "";
		   int i = 0;
		   while(decoder.get("L_LONGMESSAGE"+i) != null && !decoder.get("L_LONGMESSAGE"+i).equals(""))
		   {
		   strLongMessage = decoder.get("L_LONGMESSAGE"+i) + "  ;  ";

		   %>
		<tr>
		  <td>Error Number:</td>
		  <td><%= decoder.get("L_ERRORCODE"+i) %></td>
		</tr>
		<tr>
		  <td>Short Message:</td>
		  <td><%= decoder.get("L_SHORTMESSAGE"+i) %></td>
		</tr>
		<tr>
		  <td>Long Message:</td>
		  <td><%= decoder.get("L_LONGMESSAGE"+i) %></td>
		</tr>
		<%
		   i++;
		   }
		   %>
	      </table>
	    </center>

	  </div>
	</div>
      </div>
    </div>
  </body>
</html>
