#-------------------------------------------------------------------------------
# Copyright � 2017-2019 Harvard Pilgrim Health Care Institute (HPHCI) and its Contributors. 
# Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
# 
# The above copyright notice and this permission notice shall be included in all copies or substantial
# portions of the Software.
# 
# Funding Source: Food and Drug Administration (?Funding Agency?) effective 18 September 2014 as
# Contract no. HHSF22320140030I/HHSF22301006T (the ?Prime Contract?).
# 
# THE SOFTWARE IS PROVIDED "AS IS" ,WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
# INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
# PURPOSE AND NON-INFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
# LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT
# OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
# OTHER DEALINGS IN THE SOFTWARE.
#-------------------------------------------------------------------------------
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@page session="true"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<!DOCTYPE html>
<html class="overflow-hidden">
<head>
<!-- Basic -->
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
            
    <title>FDA MSMP</title>	
    
    <meta name="description" content="">
    <meta name="keywords" content="">
    <meta name="author" content="">

    <!-- Favicon -->
    <link rel="shortcut icon" href="/fdahpStudyDesigner/images/icons/fav.png" type="image/x-icon" />
    <link rel="apple-touch-icon" href="/fdahpStudyDesigner/images/icons/fav.png">
        
    <!-- Mobile Metas -->
    <meta name="viewport" content="width=device-width, minimum-scale=1.0, maximum-scale=1.0, user-scalable=no">
        
    <!-- Web Fonts  -->
   <link href="https://fonts.googleapis.com/css?family=Roboto:300,400" rel="stylesheet">
        
    <!-- Vendor CSS -->
    <link rel="stylesheet" href="/fdahpStudyDesigner/vendor/boostrap/bootstrap.min.css">
    <link rel="stylesheet" href="/fdahpStudyDesigner/vendor/datatable/css/dataTables.bootstrap.min.css">
    <link rel="stylesheet" href="/fdahpStudyDesigner/vendor/datatable/css/jquery.dataTables.min.css">
    
     <!-- Your custom styles (optional) -->
    <link href="/fdahpStudyDesigner/css/loader.css" rel="stylesheet">
    
    <link rel="stylesheet" href="/fdahpStudyDesigner/vendor/datatable/css/rowReorder.dataTables.min.css">
    <link rel="stylesheet" href="/fdahpStudyDesigner/vendor/dragula/dragula.min.css">
    <link rel="stylesheet" href="/fdahpStudyDesigner/vendor/magnific-popup/magnific-popup.css">
    <link rel="stylesheet" href="/fdahpStudyDesigner/vendor/font-awesome/font-awesome.min.css"> 
    <link rel="stylesheet" href="/fdahpStudyDesigner/vendor/select2/bootstrap-select.min.css">  
    <link rel="stylesheet" href="/fdahpStudyDesigner/vendor/select2/bootstrap-multiselect.css">      
    <link rel="stylesheet" href="/fdahpStudyDesigner/vendor/animation/animate.css">
        
    <!-- Theme Responsive CSS -->
    <link rel="stylesheet" href="/fdahpStudyDesigner/css/layout.css">   
        
    <!-- Theme CSS -->
    <link rel="stylesheet" href="/fdahpStudyDesigner/css/theme.css">
    <link rel="stylesheet" href="/fdahpStudyDesigner/css/style.css">
    <link rel="stylesheet" href="/fdahpStudyDesigner/css/jquery-password-validator.css"></link>
    <link rel="stylesheet" href="/fdahpStudyDesigner/css/sprites_icon.css">
        
    <!-- Head Libs -->
    <script src="/fdahpStudyDesigner/vendor/modernizr/modernizr.js"></script>
    
    <!-- Vendor -->
    <script src="/fdahpStudyDesigner/vendor/jquery/jquery-3.1.1.min.js"></script>
    <script src="/fdahpStudyDesigner/vendor/boostrap/bootstrap.min.js"></script>
    <script src="/fdahpStudyDesigner/js/validator.min.js"></script>
    <script src="/fdahpStudyDesigner/vendor/animation/wow.min.js"></script>
    <script src="/fdahpStudyDesigner/vendor/datatable/js/jquery.dataTables.min.js"></script>
     <script src="/fdahpStudyDesigner/vendor/datatable/js/dataTables.rowReorder.min.js"></script>
    <script src="/fdahpStudyDesigner/vendor/dragula/react-dragula.min.js"></script>
    <script src="/fdahpStudyDesigner/vendor/magnific-popup/jquery.magnific-popup.min.js"></script>    
    <script src="/fdahpStudyDesigner/vendor/slimscroll/jquery.slimscroll.min.js"></script>
    <script src="/fdahpStudyDesigner/vendor/select2/bootstrap-select.min.js"></script>
    
    
    <script src="/fdahpStudyDesigner/js/jquery.password-validator.js"></script>
    
	<script src="/fdahpStudyDesigner/js/underscore-min.js"></script>
     <script type="text/javascript" src="/fdahpStudyDesigner/js/loader.js"></script>
    
    
    <script>
      (function(i,s,o,g,r,a,m){i['GoogleAnalyticsObject']=r;i[r]=i[r]||function(){
      (i[r].q=i[r].q||[]).push(arguments)},i[r].l=1*new Date();a=s.createElement(o),
      m=s.getElementsByTagName(o)[0];a.async=1;a.src=g;m.parentNode.insertBefore(a,m)
      })(window,document,'script','//www.google-analytics.com/analytics.js','ga');

      ga('create', 'UA-71064806-1', 'auto');
      ga('send', 'pageview');
    </script>
        
</head>
<body class="loading background__img">
  <div id="loader"><span></span></div>
  <div id="lg-container" class="lg-container">
     
    <div class="logo__ll">
      <img src="images/logo/fda-logo-w.png"/>
    </div>
    <div class="signup__container">
      <!--container-->
      <div>
            <input type="hidden" id="csrfDet" csrfParamName="${_csrf.parameterName}" csrfToken="${_csrf.token}" />
            <div class=" col-xs-12"><!--lg-register-center  -->
             <form:form id="signUpForm" data-toggle="validator"  role="form" action="addPassword.do" method="post" autocomplete="off">
             
                    <div id="errMsg" class="error_msg">${errMsg}</div>
                    <div id="sucMsg" class="suceess_msg">${sucMsg}</div>
                    <c:if test="${isValidToken}">
                    <p class="col-xs-12  text-center boxcenter mb-xlg white__text">To begin using the services on FDA and complete your account setup process, kindly use the access code provided on your email and set up your account password.</p>
                    <div class=" col-md-6 boxcenter">
                      <div class="col-xs-6">
                        <div class="mb-lg form-group">
                             <input type="text" class="input-field wow_input" id="" name="firstName" placeholder="First Name"  value="${fn:escapeXml(userBO.firstName)}" maxlength="50" required autocomplete="off"/>
                            <div class="help-block with-errors red-txt"></div>
                        </div>
                      </div>
                      <div class="col-xs-6">
                        <div class="mb-lg form-group">
                             <input type="text" class="input-field wow_input" id="" name="lastName" placeholder="Last Name"  value="${fn:escapeXml(userBO.lastName)}" maxlength="50" required autocomplete="off"/>
                            <div class="help-block with-errors red-txt"></div>
                        </div>
                      </div>
                      <div class="col-xs-6">
                        <div class="mb-lg form-group">
                             <input type="text" class="input-field wow_input validateUserEmail" name="userEmail" placeholder="Email Address"  value="${userBO.userEmail}" oldVal="${userBO.userEmail}" pattern="[a-zA-Z0-9._%+-]+@[a-z0-9.-]+\.[a-z]{2,3}$" data-pattern-error="Email address is invalid" maxlength="100" required readonly="readonly" autocomplete="off"/>
                            <div class="help-block with-errors red-txt"></div>
                        </div>
                        </div>
                        <div class="col-xs-6">
                        <div class="mb-lg form-group">
                             <input type="text" class="input-field wow_input phoneMask" id="" name="phoneNumber" placeholder="Phone Number"  value="${userBO.phoneNumber}" data-minlength="12" maxlength="12" required autocomplete="off"/>
                            <div class="help-block with-errors red-txt"></div>
                        </div>
                        </div>
                        <div class="col-xs-12">
                        <div class="mb-lg form-group">
                             <input autofocus="autofocus" type="text" class="input-field wow_input" id="" name="accessCode"  maxlength="6" placeholder="Access Code" data-error="Access Code is invalid" required autocomplete="off"/>
                            <div class="help-block with-errors red-txt"></div>
                        </div>
                        </div>
                        <div class="col-xs-6">
                        <div class="mb-lg form-group">
                            <input type="password" class="input-field wow_input" id="password"  maxlength="64"  data-minlength="8" placeholder="Password"  required
                        pattern="^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[!&quot;#$%&amp;'()*+,-.:;&lt;=&gt;?@[\]^_`{|}~])[A-Za-z\d!&quot;#$%&amp;'()*+,-.:;&lt;=&gt;?@[\]^_`{|}~]{8,64}" autocomplete="off" data-error="Password is invalid" />
                        <div class="help-block with-errors red-txt"></div>
                        <span class="arrowLeftSugg"></span>
                            
                        </div>
                        </div>
                        <div class="col-xs-6">
                        <div class="mb-lg form-group">
                            <input type="password" class="input-field wow_input" id="cfnPassword" name=""  maxlength="64" data-match="#password" data-match-error="Whoops, these don't match" placeholder="Confirm password" 
                              required  autocomplete="off"/> 
                            <div class="help-block with-errors red-txt"></div>
                        </div>
                        </div>
                        <div class="col-xs-12">
                        <div class="mb-lg form-group">
                             <span class="checkbox checkbox-inline">
                                <input type="checkbox" id="inlineCheckbox" value="option1" required="required">
                                <label for="inlineCheckbox">
                                  <span class="white__text">I agree to the <a href="https://www.fda.gov/AboutFDA/AboutThisWebsite/WebsitePolicies/" class="grey__text" target="_blank">Terms</a> and 
                                  <a href="https://www.fda.gov/AboutFDA/AboutThisWebsite/WebsitePolicies/#privacy" class="grey__text" target="_blank">Privacy Policy</a> associated with using this portal</span>
                                </label>
                            </span> 
                            <div class="help-block with-errors red-txt"></div>
                        </div>
                        </div>
                        <div class="clearfix"></div>
                        <div class="mb-lg form-group text-center col-md-4 col-lg-4 boxcenter">
                             <button type="button" class="btn lg-btn" id="signPasswordBut">Submit</button>
                        </div>
                        </c:if>
                        <c:if test="${not isValidToken}"><p class="passwordExp text-center"><i class="fa fa-exclamation-circle" aria-hidden="true"></i>The Password Reset Link is either expired or invalid.</p></c:if>
                        <!-- <div class="text-center">
                            <a id="login" class="gray-link backToLogin white__text" href="javascript:void(0)">Back to Sign in</a>
                        </div> -->
                   </div>
                   <input type="hidden" name="securityToken" value="${securityToken}" />
                   <input type="password" name="password" id="hidePass" style="display: none;" />
                </form:form>
                </div>
                <!--container-->
                <div class="footer">
                    <span>Copyright � 2017 FDA</span><span><a href="https://www.fda.gov/AboutFDA/AboutThisWebsite/WebsitePolicies/" id="" target="_blank">Terms</a></span><span><a href="https://www.fda.gov/AboutFDA/AboutThisWebsite/WebsitePolicies/#privacy" id="" target="_blank">Privacy Policy</a></span>
                </div>  
              </div>    
  </div>
    
    <!-- Modal -->
<div class="modal fade" id="termsModal" role="dialog">
   <div class="modal-dialog modal-lg">
      <!-- Modal content-->
      <div class="modal-content">
      
      <div class="modal-header cust-hdr">
        <button type="button" class="close pull-right" data-dismiss="modal">&times;</button>       
      </div>
      <div class="modal-body pt-xs pb-lg pl-xlg pr-xlg">
      		 <div>
      			<div class="mt-md mb-md"><u><b>Terms</b></u></div>
		               <span>${masterDataBO.termsText}</span>
            </div>
      </div>
      </div>
   </div>
</div>

<div class="modal fade" id="privacyModal" role="dialog">
   <div class="modal-dialog modal-lg">
      <!-- Modal content-->
      <div class="modal-content">
      
      <div class="modal-header cust-hdr">
        <button type="button" class="close pull-right" data-dismiss="modal">&times;</button>       
      </div>
      
      <div class="modal-body pt-xs pb-lg pl-xlg pr-xlg">
      		 <div>
      			<div class="mt-md mb-md"><u><b>Privacy Policy</b></u></div>
		               <span>${masterDataBO.privacyPolicyText}</span>
            </div>
      </div>
      </div>
   </div>
</div>

    <form:form action="/fdahpStudyDesigner/login.do" id="backToLoginForm" name="backToLoginForm" method="post">
	</form:form>
    
    <script src="/fdahpStudyDesigner/js/theme.js"></script>
    <script src="/fdahpStudyDesigner/js/jquery.mask.min.js"></script>
    <script src="/fdahpStudyDesigner/js/common.js"></script>
    <script src="/fdahpStudyDesigner/js/jquery.nicescroll.min.js"></script>
    <script src="/fdahpStudyDesigner/vendor/tinymce/tinymce.min.js"></script>
    <script src="/fdahpStudyDesigner/js/bootbox.min.js"></script>
   
   
   <script>
    	$(document).ready(function(e) {
    		
    		$('.terms').on('click',function(){
    			$('#termsModal').modal('show');
    		});
    		
    		$('.privacy').on('click',function(){
    			$('#privacyModal').modal('show');
    		});
    		
    		addPasswordPopup();
    		$('.backToLogin').on('click',function(){
				$('#backToLoginForm').submit();
			});
    		
    		var errMsg = '${errMsg}';
			if(errMsg.length > 0){
				$("#errMsg").html(errMsg);
			   	$("#errMsg").show("fast");
			   	setTimeout(hideDisplayMessage, 4000);
			}
			var sucMsg = '${sucMsg}';
			if(sucMsg.length > 0){
				$("#sucMsg").html(sucMsg);
		    	$("#sucMsg").show("fast");
		    	$("#errMsg").hide("fast");
		    	setTimeout(hideDisplayMessage, 4000);
			}
			});
    	function hideDisplayMessage(){
			$('#sucMsg').hide();
			$('#errMsg').hide();
		}
    	window.onload = function () {
		    if (typeof history.pushState === "function") {
		        history.pushState("jibberish", null, null);
		        window.onpopstate = function () {
		            history.pushState('newjibberish', null, null);
		        };
		    }
		    else {
		        var ignoreHashChange = true;
		        window.onhashchange = function () {
		            if (!ignoreHashChange) {
		                ignoreHashChange = true;
		                window.location.hash = Math.random();
		            }
		            else {
		                ignoreHashChange = false;   
		            }
		        };
		    }
		}
    	
    	var addPasswordPopup = function() {
      		 $("#password").passwordValidator({
      				require: ['length', 'lower', 'upper', 'digit','spacial'],
      				length: 8
      			});
      		}
    </script>

</body>
</html>
