package com.example.readinglistapp.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.readinglistapp.auth.AuthManager
import com.example.readinglistapp.databinding.FragmentAuthBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch
import java.security.MessageDigest

class AuthFragment : Fragment() {

    private var _binding: FragmentAuthBinding? = null
    private val binding get() = _binding!!

    private lateinit var authManager: AuthManager

    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            val idToken = account?.idToken
            if (idToken != null) {
                authManager.signInWithGoogleIdToken(
                    idToken = idToken,
                    userEmailHint = account.email,
                    onSuccess = {
                        Toast.makeText(requireContext(), "Google Sign-In successful!", Toast.LENGTH_SHORT).show()
                        updateAuthState()
                    },
                    onFailure = { errorMsg ->
                        Toast.makeText(requireContext(), "Google Sign-In error: $errorMsg", Toast.LENGTH_LONG).show()
                    }
                )
            } else {
                Toast.makeText(requireContext(), "No ID token returned from Google", Toast.LENGTH_SHORT).show()
            }
        } catch (e: ApiException) {
            handleGoogleSignInError(e)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAuthBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        authManager = AuthManager.getInstance(requireContext())

        setupUI()
        updateAuthState()
    }

    private fun setupUI() {
        binding.btnGoogleSignIn.setOnClickListener {
            performGoogleSignIn()
        }

        binding.btnSignIn.setOnClickListener {
            val email = binding.etAuthEmail.text.toString().trim()
            val pass = binding.etAuthPassword.text.toString().trim()

            if (email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter email and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            authManager.signIn(
                email = email,
                pass = pass,
                onSuccess = {
                    Toast.makeText(requireContext(), "Sign in successful!", Toast.LENGTH_SHORT).show()
                    updateAuthState()
                },
                onFailure = { errorMsg ->
                    Toast.makeText(requireContext(), "Sign in failed: $errorMsg", Toast.LENGTH_SHORT).show()
                }
            )
        }

        binding.btnSignUp.setOnClickListener {
            val email = binding.etAuthEmail.text.toString().trim()
            val pass = binding.etAuthPassword.text.toString().trim()

            if (email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter email and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            authManager.signUp(
                email = email,
                pass = pass,
                onSuccess = {
                    Toast.makeText(requireContext(), "Account created successfully!", Toast.LENGTH_SHORT).show()
                    updateAuthState()
                },
                onFailure = { errorMsg ->
                    Toast.makeText(requireContext(), "Sign up failed: $errorMsg", Toast.LENGTH_SHORT).show()
                }
            )
        }

        binding.btnSignOut.setOnClickListener {
            authManager.signOut()
            Toast.makeText(requireContext(), "Signed out", Toast.LENGTH_SHORT).show()
            updateAuthState()
        }
    }

    private fun performGoogleSignIn() {
        // First try Credential Manager API
        val credentialManager = CredentialManager.create(requireContext())
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(AuthManager.WEB_CLIENT_ID)
            .setAutoSelectEnabled(false)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        lifecycleScope.launch {
            try {
                val result = credentialManager.getCredential(
                    request = request,
                    context = requireContext()
                )
                val credential = result.credential
                if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                    val idToken = googleIdTokenCredential.idToken
                    authManager.signInWithGoogleIdToken(
                        idToken = idToken,
                        userEmailHint = googleIdTokenCredential.id,
                        onSuccess = {
                            Toast.makeText(requireContext(), "Google Sign-In successful!", Toast.LENGTH_SHORT).show()
                            updateAuthState()
                        },
                        onFailure = { errorMsg ->
                            Toast.makeText(requireContext(), "Google Sign-In error: $errorMsg", Toast.LENGTH_SHORT).show()
                        }
                    )
                } else {
                    launchLegacyGoogleSignIn()
                }
            } catch (e: Exception) {
                // Fallback to legacy GoogleSignInClient intent chooser
                launchLegacyGoogleSignIn()
            }
        }
    }

    private fun launchLegacyGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(AuthManager.WEB_CLIENT_ID)
            .requestEmail()
            .build()

        val googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)
        googleSignInLauncher.launch(googleSignInClient.signInIntent)
    }

    private fun handleGoogleSignInError(e: ApiException) {
        val sha1 = getAppSha1(requireContext())
        val errorMsg = when (e.statusCode) {
            10 -> "Status Code 10 (Developer Error).\nThis usually happens when the SHA-1 fingerprint is missing in Firebase Console.\n\nPackage: ${requireContext().packageName}\nSHA-1: $sha1"
            12500 -> "Status Code 12500 (Google Sign-In failed). Check Play Services Google account on device."
            else -> "Error Code ${e.statusCode}: ${e.localizedMessage}"
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Google Sign-In Notice")
            .setMessage(errorMsg)
            .setPositiveButton("Copy SHA-1") { _, _ ->
                val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("SHA-1", sha1)
                clipboard.setPrimaryClip(clip)
                Toast.makeText(requireContext(), "SHA-1 copied to clipboard!", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Local Sign-In Fallback") { _, _ ->
                authManager.setLocalUser("google.user@example.com")
                updateAuthState()
            }
            .setNeutralButton("Dismiss", null)
            .show()
    }

    private fun getAppSha1(context: Context): String {
        return try {
            val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                context.packageManager.getPackageInfo(context.packageName, PackageManager.GET_SIGNING_CERTIFICATES)
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(context.packageName, PackageManager.GET_SIGNATURES)
            }
            val signatures = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.signingInfo?.apkContentsSigners
            } else {
                @Suppress("DEPRECATION")
                packageInfo.signatures
            }
            val sig = signatures?.firstOrNull()
            if (sig != null) {
                val md = MessageDigest.getInstance("SHA-1")
                val digest = md.digest(sig.toByteArray())
                digest.joinToString(":") { "%02X".format(it) }
            } else "Unavailable"
        } catch (e: Exception) {
            "Unavailable"
        }
    }

    private fun updateAuthState() {
        if (authManager.isLoggedIn) {
            binding.cardUserProfile.visibility = View.VISIBLE
            binding.cardAuthForm.visibility = View.GONE
            binding.tvUserEmail.text = authManager.currentUserEmail
        } else {
            binding.cardUserProfile.visibility = View.GONE
            binding.cardAuthForm.visibility = View.VISIBLE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
