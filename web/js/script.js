document.getElementById("loginForm").addEventListener("submit", async function (event) {

    event.preventDefault();

    const email = document.getElementById("email").value.trim();
    const password = document.getElementById("password").value;

    try {

        const response = await fetch("/lms/login", {

            method: "POST",

            headers: {
                "Content-Type": "application/x-www-form-urlencoded"
            },

            body: new URLSearchParams({
                email: email,
                password: password
            })
        });

        const result = await response.json();

        if (response.ok) {

            alert(result.message);

            window.location.href = "/lms/" + result.dashboard;

        } else {

            alert(result.message);
        }

    } catch (error) {

        console.error("Login error:", error);

        alert("Unable to connect to the server.");
    }
});