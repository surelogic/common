
<%@ page import="com.paypal.sdk.services.NVPCallerServices" %>
<%@ page import="com.paypal.sdk.util.*" %>
<%@ page import="com.paypal.sdk.core.nvp.NVPEncoder" %>
<%@ page import="com.paypal.sdk.core.nvp.NVPDecoder" %>
<%@ page language="java" %>
<html>
  <head>
    <title>Review Checkout Details</title>
    <meta name="title" content="Review Checkout Details" />
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
	    <%
	       NVPDecoder decoder = (NVPDecoder) session.getAttribute("response");
	       %>
	    <center>
	      <form action="confirm" method="POST">
		<table width =400>
		  <tr>
		    <td>
		      <b>Order Total:</b></td>
		    <td>
		      <%=  session.getAttribute("currencyCodeType") + " " +  session.getAttribute("paymentAmount") %>
		    </td>
		  </tr>
		  <tr>
		    <td>
		      <b>Shipping Address:</b></td>
		    <td>
		  </tr>
		  <tr>
		    <td >
		      Street 1:</td>
		    <td>
		      <%=decoder.get("SHIPTOSTREET")%></td>
		  </tr>
		  <tr>
		    <td >
		      Street 2:</td>
		    <td>
		      <%=decoder.get("SHIPTOSTREET2")%>
		    </td>
		  </tr>
		  <tr>
		    <td >
		      City:</td>
		    <td>
		      <%=decoder.get("SHIPTOCITY")%></td>
		  </tr>
		  <tr>
		    <td >
		      State:</td>
		    <td>
		      <%=decoder.get("SHIPTOSTATE")%></td>
		  </tr>
		  <tr>
		    <td >
		      Postal code:</td>
		    <td>
		      <%=decoder.get("SHIPTOZIP")%></td>
		  </tr>
		  <tr>
		    <td >
		      Country:</td>
		    <td>
		      <%=decoder.get("SHIPTOCOUNTRYNAME")%></td>
		  </tr>
		  <tr>
		    <td colspan="2" class="header">
			<input type="submit" value="Pay" />
			<input type="hidden" name="token" value="<%=decoder.get("TOKEN")%>"/>
			<input type="hidden" name="PayerID" value="<%= decoder.get("PAYERID")%>"/>
			
		    </td>
		  </tr>
		</table>
	      </form>
	    </center>
	  </div>
	</div>
      </div>
    </div>
  </body>
</html>
