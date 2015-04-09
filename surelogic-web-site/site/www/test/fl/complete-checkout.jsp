<%@ page language="java" %>
<%@ page import="com.paypal.sdk.core.nvp.NVPDecoder" %>
<html>
  <head>
    <title>Review Checkout Details</title>
    <meta name="title" content="Complete Checkout" />
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
	    <% NVPDecoder decoder = (NVPDecoder) session.getAttribute("response"); %>
	    <b>Thank you for your payment! An email should have been sent to
	    you containing the license file you will need in order to use the
	    flashlight plugin.</b><br><br>
	    <table width = 400>
              
              <tr>
		<td>
                  Transaction ID:</td>
		<td>
                  <%= decoder.get("TRANSACTIONID")%></td>
              </tr>
              <tr>
		<td >
                  Amount:</td>
		<td>
            	  <%= decoder.get("CURRENCYCODE") + " " +  decoder.get("AMT") %> </td>
              </tr>
	    </table>
	  </div>
	</div>
      </div>
    </div>
  </body>
</html>


