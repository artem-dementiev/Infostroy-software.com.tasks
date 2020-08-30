<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Login</title>
    <link rel="stylesheet" href="Css/styleLogin.css">
</head>
<body>
<div class="container">
    <div class="start">

        <label for="username"> Your name: </label>
        <input type="text" id="username" name="username" required placeholder="Enter your name...">
        <button id="logIn">Log In</button>
        <div class="errorSpan" style="color: red"></div>
    </div>

    <div class="classroom">
        <nav>
            <div class="navLeft">
                <ul>
                    <li>
                        <a href="#">Actions <img src="img/angle-down-solid.svg" alt=""></a>
                        <ul>
                            <li>
                                <a href="#" id="raise">Raise hand up</a>
                            </li>
                        </ul>
                    </li>
                </ul>
            </div>
            <div class="navCenter"></div>
            <div class="navRight">
                <div>
                    <ul>
                        <li><a href="#" id="user">Unknown</a>
                            <ul>
                                <li>
                                    <a href="#" id="logOut">Log Out</a>
                                </li>
                            </ul>
                        </li>
                    </ul>
                </div>
            </div>
        </nav>
        <div class="userbox">
            <h4>Class members</h4>
            <div class="messages">

            </div>
        </div>
    </div>
</div>
<script src="Js/scriptLogin.js"></script>
</body>
</html>
