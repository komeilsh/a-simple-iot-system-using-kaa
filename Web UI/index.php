
<?php

session_start();
if (!isset($_SESSION['username'])){
      header("location:login.php"); ;

 }

$client = new MongoClient("mongodb://194.225.227.171:27017");  
$db = $client->kaa;

$log = $db->logs_91832476309660881037;
$profile = $db->endpoint_profile;


$devices = $profile->find(array('application_id'=>'14'));

if (isset($_GET['x'])){
	$selected = $_GET['x'];
	
	$sel_dev = $profile->find(array('profile.phoneId'=>$selected));
	
	foreach ($sel_dev as $iterator) {
			$pure_device = $iterator;
			$selectedd = $iterator['profile'];
			//$selected = $selectedd['phoneId'];
			break;
	}
}
else{
	foreach ($devices as $iterator) {
				$pure_device = $iterator;
				$selectedd = $iterator['profile'];
				$selected = $selectedd['phoneId'];
				break;
			}
}

$empty_flag=false;

$cursor= $log->find(array('event.phoneId' => $selected));
$logs_count = $cursor->count();
$cursor->sort(array('header.timestamp.long' => -1));
$cursor->limit(15);

$signal_array = [];
$counter=0;
foreach ($cursor as $iterator) {
	$log_event = $iterator['event'];
	$signal_value=$log_event['signalStrength'];
	if ($signal_value<0){
		$signal_value=0;
        }
	
	array_push($signal_array, $signal_value);
	//$counter++;
	
}

if ($logs_count==0){
	$empty_flag=true;
}
else{
foreach ($cursor as $iterator) {
	$pure_last_log = $iterator;
	$last_log = $iterator['event'];
	break;
}
$GPS_loc = $last_log['phoneGpsLocation'];
$lat = $GPS_loc['latitude'];


$lng = $GPS_loc['longitude'];


//echo $counter;






}
?>






<!DOCTYPE html>

<html lang="en"><head><meta http-equiv="Content-Type" content="text/html; charset=UTF-8">

<meta name="viewport" content="width=device-width, initial-scale=1.0">

<meta name="description" content="display info of iot">

    <title>IOT Display</title>


<link rel="stylesheet" href="pure-min.css">



<link rel="stylesheet" href="css">
<link rel="stylesheet" href="1.18.13">
   
<script
src="http://maps.googleapis.com/maps/api/js?key=AIzaSyDrn9vqb3qnV6AzZNJ5_fI0A3sOOa5sVHs">
</script>
  <script>

var myCenter=new google.maps.LatLng('<?php echo $lat;?>','<?php echo $lng;?>');

function initialize()
{
var mapProp = {
  center:myCenter,
  zoom:15,
  mapTypeId:google.maps.MapTypeId.ROADMAP
  };

var map=new google.maps.Map(document.getElementById("googleMap"),mapProp);

var marker=new google.maps.Marker({
  position:myCenter,
  });

marker.setMap(map);
}

google.maps.event.addDomListener(window, 'load', initialize);
</script>
   
<script type="text/javascript" src="js/fusioncharts.js"></script>
<script type="text/javascript" src="js/themes/fusioncharts.theme.fint.js"></script>   
   <script type="text/javascript">
  FusionCharts.ready(function(){
    var revenueChart = new FusionCharts({
        "type": "column2d",
        "renderAt": "chartContainer",
        "width": "500",
        "height": "400",
        "dataFormat": "json",
        "dataSource":  {
          "chart": {
            "caption": "Last 15 Received Signal Strengthes",
            "subCaption": "kaa IOT",
            "xAxisName": "step",
            "yAxisName": "signal strength",
            "theme": "fint"
         },
         "data": [
            {
               "label": "1st sent",
               "value": '<?php echo $signal_array[14];?>'
            },
            {
                "label": "2st sent",
               "value": '<?php echo $signal_array[13];?>'
            },
            {
                "label": "3st sent",
               "value": '<?php echo $signal_array[12];?>'
            },
            {
                "label": "4st sent",
               "value": '<?php echo $signal_array[11];?>'
            },
            {
                "label": "5st sent",
               "value": '<?php echo $signal_array[10];?>'
            },
            {
                "label": "6st sent",
               "value": '<?php echo $signal_array[9];?>'
            },
            {
                "label": "7st sent",
               "value": '<?php echo $signal_array[8];?>'
            },
            {
                "label": "8st sent",
               "value": '<?php echo $signal_array[7];?>'
            },
            {
                "label": "9st sent",
               "value": '<?php echo $signal_array[6];?>'
            },
            {
                "label": "10st sent",
               "value": '<?php echo $signal_array[5];?>'
            },
            {
                "label": "11st sent",
               "value": '<?php echo $signal_array[4];?>'
            },
            {
                "label": "12st sent",
               "value": '<?php echo $signal_array[3];?>'
            },
			{
               "label": "13st sent",
               "value": '<?php echo $signal_array[2];?>'
            },
			{
               "label": "14st sent",
               "value": '<?php echo $signal_array[1];?>'
            },
			{
               "label": "15st sent",
               "value": '<?php echo $signal_array[0];?>'
            }
          ]
      }

  });
revenueChart.render();
})
</script>
   
   

<script async="" src="/analytics.js"></script><script>
(function(i,s,o,g,r,a,m){i['GoogleAnalyticsObject']=r;i[r]=i[r]||function(){
(i[r].q=i[r].q||[]).push(arguments)},i[r].l=1*new Date();a=s.createElement(o),
m=s.getElementsByTagName(o)[0];a.async=1;a.src=g;m.parentNode.insertBefore(a,m)
})(window,document,'script','//www.google-analytics.com/analytics.js','ga');

ga('create', 'UA-41480445-1', 'purecss.io');
ga('send', 'pageview');
</script>


<style id="holderjs-style" type="text/css"></style><script type="text/javascript" async="" src="init_158_236_.js"></script></head>
<body class=" __plain_text_READY__">

    <script>
(function (root) {
// -- Data --
root.YUI_config = {"version":"3.17.2","base":"http:\u002F\u002Fyui.yahooapis.com\u002F3.17.2\u002F","comboBase":"http:\u002F\u002Fyui.yahooapis.com\u002Fcombo?","comboSep":"&","root":"3.17.2\u002F","filter":"min","logLevel":"error","combine":true,"patches":[],"maxURLLength":2048,"groups":{"vendor":{"combine":true,"comboBase":"\u002Fcombo\u002F1.18.13?","base":"\u002F","root":"\u002F","modules":{"css-mediaquery":{"path":"vendor\u002Fcss-mediaquery.js"},"handlebars-runtime":{"path":"vendor\u002Fhandlebars.runtime.js"}}},"app":{"combine":true,"comboBase":"\u002Fcombo\u002F1.18.13?","base":"\u002Fjs\u002F","root":"\u002Fjs\u002F"}}};
root.app || (root.app = {});
root.app.yui = {"use":function () { return this._bootstrap('use', [].slice.call(arguments)); },"require":function () { this._bootstrap('require', [].slice.call(arguments)); },"ready":function (callback) { this.use(function () { callback(); }); },"_bootstrap":function bootstrap(method, args) { var self = this, d = document, head = d.getElementsByTagName('head')[0], ie = /MSIE/.test(navigator.userAgent), callback = [], config = typeof YUI_config != "undefined" ? YUI_config : {}; function flush() { var l = callback.length, i; if (!self.YUI && typeof YUI == "undefined") { throw new Error("YUI was not injected correctly!"); } self.YUI = self.YUI || YUI; for (i = 0; i < l; i++) { callback.shift()(); } } function decrementRequestPending() { self._pending--; if (self._pending <= 0) { setTimeout(flush, 0); } else { load(); } } function createScriptNode(src) { var node = d.createElement('script'); if (node.async) { node.async = false; } if (ie) { node.onreadystatechange = function () { if (/loaded|complete/.test(this.readyState)) { this.onreadystatechange = null; decrementRequestPending(); } }; } else { node.onload = node.onerror = decrementRequestPending; } node.setAttribute('src', src); return node; } function load() { if (!config.seed) { throw new Error('YUI_config.seed array is required.'); } var seed = config.seed, l = seed.length, i, node; if (!self._injected) { self._injected = true; self._pending = seed.length; } for (i = 0; i < l; i++) { node = createScriptNode(seed.shift()); head.appendChild(node); if (node.async !== false) { break; } } } callback.push(function () { var i; if (!self._Y) { self.YUI.Env.core.push.apply(self.YUI.Env.core, config.extendedCore || []); self._Y = self.YUI(); self.use = self._Y.use; if (config.patches && config.patches.length) { for (i = 0; i < config.patches.length; i += 1) { config.patches[i](self._Y, self._Y.Env._loader); } } } self._Y[method].apply(self._Y, args); }); self.YUI = self.YUI || (typeof YUI != "undefined" ? YUI : null); if (!self.YUI && !self._injected) { load(); } else if (self._pending <= 0) { setTimeout(flush, 0); } return this; }};
root.YUI_config || (root.YUI_config = {});
root.YUI_config.seed = ["http:\u002F\u002Fyui.yahooapis.com\u002Fcombo?3.17.2\u002Fyui\u002Fyui-min.js"];
root.YUI_config.groups || (root.YUI_config.groups = {});
root.YUI_config.groups.app || (root.YUI_config.groups.app = {});
root.YUI_config.groups.app.modules = {"start\u002Fapp":{"path":"start\u002Fapp.js","requires":["handlebars-runtime","yui","base-build","router","pjax-base","view","start\u002Fmodels\u002Fgrid","start\u002Fviews\u002Finput","start\u002Fviews\u002Foutput","start\u002Fviews\u002Fdownload"]},"start\u002Fmodels\u002Fgrid":{"path":"start\u002Fmodels\u002Fgrid.js","requires":["yui","querystring","base-build","model","model-sync-rest","start\u002Fmodels\u002Fmq"]},"start\u002Fmodels\u002Fmq":{"path":"start\u002Fmodels\u002Fmq.js","requires":["css-mediaquery","attribute","base-build","model","model-list"]},"start\u002Fviews\u002Fdownload":{"path":"start\u002Fviews\u002Fdownload.js","requires":["yui","base-build","querystring","view"]},"start\u002Fviews\u002Finput":{"path":"start\u002Fviews\u002Finput.js","requires":["base-build","start\u002Fmodels\u002Fmq","start\u002Fviews\u002Ftab"]},"start\u002Fviews\u002Foutput":{"path":"start\u002Fviews\u002Foutput.js","requires":["base-build","escape","start\u002Fviews\u002Ftab"]},"start\u002Fviews\u002Ftab":{"path":"start\u002Fviews\u002Ftab.js","requires":["yui","base-build","view"]}};
}(this));
</script>
<script type="text/javascript" src="js/fusioncharts.js"></script>
<script type="text/javascript" src="js/themes/fusioncharts.theme.fint.js"></script>




    <div id="layout">
 
<div id="menu" style="width:20%">
    <div class="pure-menu" >
        <a class="pure-menu-heading" href="#">Devices</a>
        <ul class="pure-menu-list">
          <?php
			
			foreach ($devices as $document) {
				$selectedd_ = $document['profile'];
				$selected_ = $selectedd_['phoneId'];
				print("<li class='pure-menu-item'><a href='?x=$selected_' class='pure-menu-link'>IMEI : $selected_</a></li>");
			}
		  
		  
		  ?>
		 
        </ul>
    </div>
</div>

        <div id="main">


<div class="header">
    <h1>Phone Info</h1>

    <h2>last information from this GateWay</h2>
  
</div>

<div class="content">


    <h2 id="table-with-horizontal-borders" class="content-subhead">Client Side EndPoint Profile</h2>
  

    <table class="pure-table pure-table-horizontal">


    <tbody>
        <tr>
            
            <td>Phone ID</td>
			
            <td><?php print("$selected") ?></td>
           
        </tr>

		
		<tr class="pure-table-odd">
		 
            <td>EndPoint KeyHash</td>
			<?php	
			$header_object = $pure_last_log['header'];
			$endpoint_key_ = $header_object['endpointKeyHash'];
			$endpoint_key = $endpoint_key_['string'];
			//echo var_dump($endpoint_key);
            print("<td>$endpoint_key</td>");
           ?>
		   
        </tr>
		
			<tr >
		 
            <td>Count of Logs</td>
			<?php	
			
            print("<td>$logs_count</td>");
           ?>
		   
        </tr>
		
		
        <tr class="pure-table-odd">
		
            <td>Os Version</td>
			<?php
			$os_ver_code = $selectedd['os_version'];
			$os_version="";
			switch ($os_ver_code) {
			case "21":
				$os_version="Lollipop	5.0";
				break;
			case "22":
				$os_version="Lollipop	5.1";
				break;
			case "23":
				$os_version="Marshmallow	6.0";
				break;
			case "24":
				$os_version="Nougat	 7.0";
				break;
			case "19":
				$os_version="KitKat	4.4";
				break;
			case "18":
				$os_version="Jelly Bean	4.3";
				break;
			case "17":
				$os_version="Jelly Bean	4.2";
				break;
			default:
				$os_version=$os_ver_code;
}

			
            print ("<td>$os_version</td>");
           ?>
        </tr>
		
		 
		
		 <tr >
		 
            <td>Profile Version</td>
			<?php	
			$profile_ver = $pure_device['profile_version'];
            print("<td>version $profile_ver</td>");
           ?>
		   
        </tr>

    </tbody>
</table>


 

    <h2 id="table-with-horizontal-borders" class="content-subhead">The Last Received Log</h2>
    
<table class="pure-table pure-table-horizontal">


    <tbody>
	
	<tr class="pure-table-odd">
            
            <td>Time of receive</td>
			
            <td><?php 
			
			$time1 = $pure_last_log['header'];
			$time2 = $time1['timestamp'];
			$time3 = $time2['long'];
			//var_dump($time3)		
			$time_string = (string)$time3;
			$timestamp=substr($time_string, 0, 10);
			$timestamp_int=(int)$timestamp;
			//$timestamp_int +=23400; 
			//var_dump($timestamp_int);
			$time4 = date("d F Y H:i:s",$timestamp_int);
			print("$time4") ;
			?>
			</td>
           
        </tr>
		
	
        <tr>
            
            <td>Network Operation Code</td>
            <?php
				if ($empty_flag==true){
					$temp_="Unknown";
				}
				else{
				$temp_=$last_log['networkOperatorCode'];}
				print("<td>$temp_</td>");
           ?>
           
        </tr>

        <tr class="pure-table-odd">
            <td>Network Operation Name</td>
             <?php
			 if ($empty_flag==true){
					$temp_="Unknown";
				}
				else{
				$temp_=$last_log['networkOperatorName'];}
				
				print("<td>$temp_</td>");
           ?>
           
        </tr>
		
		<tr>
            
            <td>GSM Lac</td>
             <?php
				if ($empty_flag==true){
					$temp_="Unknown";
				}
				else{
				$temp_=$last_log['gsmLac'];}
				print("<td>$temp_</td>");
           ?>
           
        </tr>
		
		<tr class="pure-table-odd">
            
            <td>Signal Strength</td>
            <?php
				if ($empty_flag==true){
					$temp_="Unknown";
				}
				else{
				$temp_=$last_log['signalStrength'];}
				print("<td>$temp_</td>");
           ?>
          
		    </tr>
		  
		  <tr> 
		   <td>Temperature</td>
            <?php
				
				$temp_=$last_log['arrayField'];
				$temp_humidty = $temp_[3];
				$splited = explode(",", $temp_humidty);
				if (sizeof($splited)==2){
				$temperature = $splited[0];
				//echo var_dump($splited);
				print("<td>$temperature</td>");}
				else{
					print("<td>Unknown</td>");
				}
           ?>
		   
		   
        </tr >
		
		 <tr class="pure-table-odd"> 
		   <td>Humidity</td>
            <?php
				if (sizeof($splited)==2){
				$humidity = $splited[1];
				print("<td>$humidity</td>");}
				else{
					print("<td>Unknown</td>");
				}
           ?>
		   
		   
        </tr >
		
		<tr >
            
            <td>acceleration</td>
            <?php
				
				$temp_=$last_log['arrayField'];
				$acc1 = $temp_[0];
				$acc2 = $temp_[1];
				$acc3 = $temp_[2];
				//echo var_dump($acc3);
				print("<td>$acc1 , $acc2 , $acc3</td>");
           ?>
           
        </tr>
		
	

    </tbody>
</table>








<h2 id="table-with-horizontal-borders" class="content-subhead">Last 15 received signal</h2>
<div id="chartContainer"  >Chart will load here!</div>


<h2 id="table-with-horizontal-borders" class="content-subhead">Location of EndPoint</h2>


<div id="googleMap" style="width:500px;height:380px;"></div>


    

            <div class="footer">
                <div class="legal pure-g">
    <div class="pure-u-1 u-sm-1-2">
        <p class="legal-license">
            This site is built to show information of Cell Monitor Application and 
            All source code on this project is reserved </a> unless otherwise stated.
        </p>
    </div>

    <div class="pure-u-1 u-sm-1-2">
        <ul class="legal-links">
            <li><a href="#">GitHub Project</a></li>
            <li><a href="signout.php">Sign out</a></li>
        </ul>

        <p class="legal-copyright">
            © 2016 kaa Inc. All rights reserved.
        </p>
    </div>
</div>

            </div>
        </div>
    </div>
  
    <script src="1.18(1).13"></script>
  



</body></html>



