package edu.uvg.com.example.laboratorio9

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.ktx.Firebase

class AuthActivity : AppCompatActivity() {
    private val GOOGLE_SIGN_IN = 100
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
        session()

    }


    override fun onStart() {
        super.onStart()
        val authLayout = findViewById<LinearLayout>(R.id.authLayout)
        authLayout.visibility = View.VISIBLE
    }

    private fun session() {

        val prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE)
        val email = prefs.getString("email", null)
        val provider = prefs.getString("provider", null)
        val authLayout = findViewById<LinearLayout>(R.id.authLayout)
        if (email != null && provider != null) {
            authLayout.visibility = View.INVISIBLE
            showHome(email, ProviderType.valueOf(provider))
        }

    }


    private fun setup() {

        // Título de autenticación
        title = "Autenticación"

        // componentes
        val signUpButton = findViewById<Button>(R.id.signUpButton)
        val logInButton = findViewById<Button>(R.id.logInButton)
        val emailEditText = findViewById<EditText>(R.id.emailEditText)
        val passwordEditText = findViewById<EditText>(R.id.passwordEditText)
        val googleButton = findViewById<Button>(R.id.googleButton)
        // Al registrar
        // Al registrar
        signUpButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()

            // Verificar que el correo y la contraseña cumplan con los requisitos
            if (email.isNotEmpty() && password.length >= 6) {
                // Autenticar el usuario
                FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            // Registro exitoso
                            showHome(task.result?.user?.email ?: "", ProviderType.BASIC)
                        } else {
                            showAlert()
                        }
                    }
            } else {
                // La contraseña es demasiado corta
                // Muestra un mensaje de error o realiza una acción adecuada
                // Por ejemplo, mostrar un Toast con un mensaje de error
                Toast.makeText(this, "La contraseña debe tener al menos 6 caracteres.", Toast.LENGTH_SHORT).show()
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
                        Log.e(
                            "ERROR AUTENTICACION",
                            "Estoy en Login, tengo email {${emailEditText.text} y password {${passwordEditText.text}}}"
                        )

                    }
                }
            }
        }

        // Login Google
        googleButton.setOnClickListener {
            //Configuración
            val googleConf = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.prefs_file)).requestEmail().build()

            val googleClient = GoogleSignIn.getClient(this, googleConf)
            googleClient.signOut()

            startActivityForResult(googleClient.signInIntent, GOOGLE_SIGN_IN)


        }


    }


    /**
     * Función para mostrar error
     * */
    private fun showAlert() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Error")
        builder.setMessage("Se ha producido un error de autenticado al usuario - 6 Caracteres en contraseña minimos")
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


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GOOGLE_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)

            try {
                val account = task.getResult(ApiException::class.java)

                if (account != null) {
                    val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                    FirebaseAuth.getInstance().signInWithCredential(credential)

                    if (FirebaseAuth.getInstance().signInWithCredential(credential).isSuccessful) {
                        showHome(account.email ?: "", ProviderType.GOOGLE)

                    } else {
                        showAlert()
                    }

                }
            } catch (e: ApiException) {

                showAlert()
            }

        }
    }


}