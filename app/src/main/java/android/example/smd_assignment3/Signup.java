package android.example.smd_assignment3;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class Signup extends AppCompatActivity     {
        private EditText edtUserName, edtPassword;
        private Button btnRegister;
        private Button btnLogin;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_signup);

            edtUserName = findViewById(R.id.idEdtUserName);
            edtPassword = findViewById(R.id.idEdtPassword);
            btnRegister = findViewById(R.id.idBtnRegister);
            btnLogin = findViewById(R.id.idBtnLogin);

            PasswordManagerDB db = new PasswordManagerDB(this);

            btnRegister.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String userName = edtUserName.getText().toString().trim();
                    String password = edtPassword.getText().toString().trim();

                    db.open();

                    if (db.userCheck(userName)){
                        Toast.makeText(Signup.this, "Username is taken!", Toast.LENGTH_LONG).show();
                    }else {

                        long records = db.addNewUser(userName, password);

                        if (records != -1) {
                            Toast.makeText(Signup.this, "Signup Successful!", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(Signup.this, "Error occurred while signing up", Toast.LENGTH_LONG).show();
                        }
                    }
                    db.close();
                }
            });



            btnLogin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(Signup.this, Login.class);
                    startActivity(intent);
                }
            });
        }





}
