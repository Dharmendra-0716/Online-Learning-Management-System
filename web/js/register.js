document.getElementById("registerForm").addEventListener("submit", async function (event) {

    event.preventDefault();

    const name = document.getElementById("name").value.trim();
    const email = document.getElementById("email").value.trim();
    const password = document.getElementById("password").value;
    const confirmPassword = document.getElementById("confirmPassword").value;

    if (password !== confirmPassword) {
        alert("Passwords do not match!");
        return;
    }

    try {

        const response = await fetch("register", {
            method: "POST",
            headers: {
                "Content-Type": "application/x-www-form-urlencoded"
            },
            body: new URLSearchParams({
                name: name,
                email: email,
                password: password,
                confirmPassword: confirmPassword
            })
        });

        const result = await response.json();

        if (response.ok) {

            alert(result.message);

            document.getElementById("registerForm").reset();

            window.location.href = "login.html";

        } else {

            alert(result.message);
        }

    } catch (error) {

        console.error("Registration error:", error);

        alert("Unable to connect to the server.");
    }
});