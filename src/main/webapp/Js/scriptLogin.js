window.addEventListener("load", e => Unit.init());
let Unit = {
    init: function () {
        this.startBox = document.querySelector(".start");
        this.nav = document.querySelector("nav");
        this.classroomBox = document.querySelector(".classroom");
        this.userBox = this.classroomBox.querySelector(".userbox");

        this.nameInput = this.startBox.querySelector("#username")
        this.startBtn = this.startBox.querySelector("#logIn");
        this.errorSpan = this.startBox.querySelector(".errorSpan");

        this.raise = this.nav.querySelector("#raise");
        this.user = this.nav.querySelector("#user");
        this.endBtn = this.nav.querySelector("#logOut");

        this.users = this.userBox.querySelector(".messages");
        this.bindEvents();
    },

    bindEvents() {
        this.wrongName=false;
        this.startBtn.addEventListener("click", e => this.validation());
        this.raise.addEventListener("click", e => {
            e.preventDefault();
            var status;
            if(this.raise.textContent === "Raise hand up"){
                this.raise.innerHTML = "Raise hand down";
                status =1;
            } else {
                this.raise.innerHTML = "Raise hand up";
                status=0;
            }

            this.send(status);

        })
        this.endBtn.addEventListener("click", e => this.onClose());
    },

    validation(){
        let nameRegExp = /^[a-zA-Z][a-zA-Z0-9_.,-]{2,31}$/;
        this.name = this.nameInput.value;
        var test = this.name.length === 0 || nameRegExp.test(this.name)|| this.name.length === null;

        if (!test) {
            this.errorSpan.innerHTML = "The name must start with a Latin character. length from 3 to 32 characters";
            return false;
        } else {
            this.openSocket()
        }
    },

    send(status) {
        this.sendMessage({
            status: status,
            action: "changeHand"
        });
    },

    onOpenSoc() {

    },

    onMessage(parse) {
        if(parse.action === "membersList" || parse.action ===  "deleteFromAndUpdateMembersList"){
            console.log("Получили список пользователей\nСписок:");
            let obj = JSON.parse(parse.memberList);
            console.log(obj);
            this.users.innerHTML ="";

            for (el in obj) {
                let block = document.createElement("div");
                block.className = "msg";
                let fromBlock = document.createElement("span");
                fromBlock.className = "from";
                fromBlock.innerText = el;

                let status = document.createElement("span");
                status.className = "status";
                if(obj[el]==="1"){
                    let img = document.createElement("img");
                    img.src = "img/hand.svg"
                    status.appendChild(img);
                }else{
                    status.innerText = "";
                }
                status.id = el;

                block.appendChild(fromBlock);
                block.appendChild(status);
                this.users.appendChild(block);
            }
            this.wrongName=false;
        } else if(parse.action === "changeHand"){
            console.log("Сменили руку на сервере");
            let str = "#"+parse.name;
            let c = this.users.querySelector(str);
            if(parse.status === "1"){
                let img = document.createElement("img");
                img.src = "img/hand.svg"
                c.appendChild(img);
            }else{
                c.innerText = "";
            }
        }
        else if(parse.action ===  "checkUnique"){
            this.errorSpan.innerHTML = "Wrong name! Choose another one!";
            this.wrongName=true;
            // window.close();
        }
        if(this.wrongName===false){
            this.startBox.style.display = "none";
            this.classroomBox.style.display = "flex";
            history.pushState(null, null, '/classroom.com/members.jsp');
        }
    },

    onClose() {
        this.sendMessage({
            action: "deleteFromAndUpdateMembersList"
        });
        this.ws.close();
        location.href = "http://localhost:8080/classroom.com/login.jsp";
    },

    onError() {
        console.log("Error!");
    },

    sendMessage(msg) {
        this.ws.send(JSON.stringify(msg));
    },

    openSocket() {
        this.name = this.nameInput.value;
        this.user.innerHTML = this.name;

        this.ws = new WebSocket("ws://localhost:8080/classroom.com/members/" + this.name);

        this.ws.onopen = () => this.onOpenSoc();
        this.ws.onmessage = (e) => this.onMessage(JSON.parse(e.data));
        this.ws.onclose = (e) => this.onClose();
        this.ws.onerror = (e) => this.onError();
    }
};
