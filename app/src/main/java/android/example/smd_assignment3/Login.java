package android.example.smd_assignment3;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class Login extends AppCompatActivity {

        private EditText edtUserNameLogin, edtPasswordLogin;
        private Button btnLogin;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_login);

            edtUserNameLogin = findViewById(R.id.idEdtUserNameLogin);
            edtPasswordLogin = findViewById(R.id.idEdtPasswordLogin);
            btnLogin = findViewById(R.id.idBtnLogin);

            PasswordManagerDB db = new PasswordManagerDB(this);

            btnLogin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String userName = edtUserNameLogin.getText().toString().trim();
                    String password = edtPasswordLogin.getText().toString().trim();

                    db.open();

                    if (!db.userCheck(userName)){
                        Toast.makeText(Login.this, "Username does not exist!", Toast.LENGTH_LONG).show();
                    }else {

                        Boolean userExists = db.login(userName, password);

                        if (userExists) {
                            Toast.makeText(Login.this, "Successfully logged in!", Toast.LENGTH_LONG).show();
                            Intent intent = new Intent(Login.this, Profile.class);
                            intent.putExtra("uname", userName);
                            startActivity(intent);
                        } else {
                            Toast.makeText(Login.this, "Wrong Password!", Toast.LENGTH_LONG).show();
                        }
                    }
                    db.close();
                }
            });
        }
    }