package edu.uvg.com.example.laboratorio9

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase

class AuthActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)

        lateinit var firebaseAnalytics: FirebaseAnalytics

        // obtener instancia
        firebaseAnalytics = Firebase.analytics

        val bundle = Bundle()
        bundle.putString("message", "Integración de Firebase Completa")
        firebaseAnalytics.logEvent("InitScreen", bundle)

        //setup
        setup()

    }

    private fun setup() {

        // Título de autenticación
        title = "Autenticación"

        // componentes
        val signUpButton = findViewById<Button>(R.id.signUpButton)
        val logInButton = findViewById<Button>(R.id.logInButton)
        val emailEditText = findViewById<EditText>(R.id.emailEditText)
        val passwordEditText = findViewById<EditText>(R.id.passwordEditText)

        // Al registrar
        signUpButton.setOnClickListener {

            // Validar que hay una contraseña o email
            if (emailEditText.text.isNotEmpty() && passwordEditText.text.isNotEmpty()) {
                // Autenticar el usuario
                FirebaseAuth.getInstance().createUserWithEmailAndPassword(
                    emailEditText.text.toString(),
                    passwordEditText.text.toString()
                    // notificación de que ha sido existoso
                ).addOnCompleteListener {
                    if (it.isSuccessful) {
                        showHome(it.result?.user?.email ?: "", ProviderType.BASIC)


                    } else {
                        showAlert()
                        Log.e("ERROR AUTENTICACION", "Estoy en Registro, tengo email {${emailEditText.text} y password {${passwordEditText.text}}}")
                    }
                }
            }
        }

        // Al acceder
        logInButton.setOnClickListener {

            // Validar que hay una contraseña o email
            if (emailEditText.text.isNotEmpty() && passwordEditText.text.isNotEmpty()) {
                // Autenticar el usuario
                FirebaseAuth.getInstance().signInWithEmailAndPassword(
                    emailEditText.text.toString(),
                    passwordEditText.text.toString()
                    // notificación de que ha sido existoso
                ).addOnCompleteListener {
                    //Log.e("ERROR AUTENTICACION", "mi valor de autenticacion es ${it.isSuccessful}}")
                    if (it.isSuccessful) {
                        showHome(it.result?.user?.email ?: "", ProviderType.BASIC)

                    } else {
                        showAlert()
                        Log.e("ERROR AUTENTICACION", "Estoy en Login, tengo email {${emailEditText.text} y password {${passwordEditText.text}}}")

                    }
                }
            }
        }


    }


    /**
     * Función para mostrar error
     * */
    private fun showAlert() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Error")
        builder.setMessage("Se ha producido un error de autenticado al usuario")
        builder.setPositiveButton("Aceptar", null)
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    /**
     * Función para mostrar successfull
     * */
    private fun showHome(email: String, provider: ProviderType) {

        // cambio de activity
        val homeIntent = Intent(this, HomeActivity::class.java).apply {

            // Mandar parámetros
            putExtra("email", email)
            putExtra("provider", provider.name)

        }

        startActivity(homeIntent)
    }


}