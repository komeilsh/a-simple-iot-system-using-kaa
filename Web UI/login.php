<?php
  session_start();
  $flag=0;
  if (isset($_POST['user_name']) && isset($_POST['password'])){
        $username = $_POST['user_name'];
        $password = $_POST['password'];
		
        
		
		$client = new MongoClient("mongodb://194.225.227.171:27017");  
		$db = $client->kaa;
		$users = $db->users;

		$query = $users->find(array('username'=>$username , 'password'=>$password));
		$count_users = $query->count();
		
        if ($count_users!=1){
           $flag='The UserName And/Or Password is incorrect!Please try again...';

	    }

        else if($count_users == 1) {
		   $_SESSION['username']=$username;
		    header("location:index.php");
	    }

        else {
         $flag='The User Name And/Or Password is incorrect!Please try again... ';}



  }


?>



<html lang="en">
    <head>
        <title>Login</title>
        <meta name="description" content="login">
        <meta name="keyword" content="Network, Log in">
        <meta name="author" content="milad72t">
        <link rel="stylesheet" type="text/css" href="style.css">
        <style>
            <!-- empty -->
        </style>
    </head>
    
    <body >
    
        <div class="form">
        <h2> Sign In </h2>
        <div class="line"> </div>
	
		<h4 style="color:red; margin-left:38px;">
      
		<?php
        if ($flag){
         print ("<font color='#FF0000'>$flag<br /></font>");
        }
	

        ?>
        </h4>
	

        <form class="input-form" action="" method="post">
            <input type="text" name="user_name" required placeholder="User Name">
            <input type="password" name="password" required placeholder="Password">
            <input type="submit" class="sign" value="Sign In">
		<p> 
            <a href="app-debug.apk">Download App now!</a> </p>            
        </form>
        </div>
    </body>
    
</html>
