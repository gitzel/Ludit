package com.example.ludit;


import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.icu.text.DateFormat;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.example.ludit.ui.filho.Filho;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FilhoCadastroActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {

    String dataDeNascimento;
    EditText edtNome,edtDeficiencia,edtTexto;
    Button btnDataNascimento;
    Button btnCadastrarFilho, imgPerfilFilho;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filho_cadastro);

        sharedPreferences = getApplicationContext().getSharedPreferences("minhaShared",MODE_PRIVATE);

        final String email = sharedPreferences.getString("email", null);

        imgPerfilFilho = findViewById(R.id.imgPerfilFilho);
        edtNome = (EditText) findViewById(R.id.edNomeFilho);
        edtDeficiencia = (EditText) findViewById(R.id.edDeficiencia);
        edtTexto = (EditText) findViewById(R.id.edTexto);
        btnDataNascimento = (Button) findViewById(R.id.btnDataDeNascimento);
        btnCadastrarFilho  = (Button)findViewById(R.id.btnCadastrarFilho);

        btnCadastrarFilho.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               cadastarFilho(email);
            }
        });

        imgPerfilFilho.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                abrirDialog();
            }
        });

        btnDataNascimento.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DataDialog dataDialog = new DataDialog();
                dataDialog.show(getSupportFragmentManager(), "Selecione a data de nascimento");
            }
        });
    }




    private void abrirDialog () {
        AlertDialog.Builder builder = new AlertDialog.Builder(FilhoCadastroActivity.this);
        View dialogView = getLayoutInflater().inflate(R.layout.layout_dialog_lista_imagens, null);
        int img = R.drawable.xvbfxb;
        final List<Integer> array = new ArrayList<>();
        array.add(img);
        array.add(img);
        array.add(img);
        array.add(img);


        ListView lvDialogImagens = dialogView.findViewById(R.id.dialogListImagens);
        ListaImagensAdapter adapter = new ListaImagensAdapter(FilhoCadastroActivity.this, array);
        lvDialogImagens.setAdapter(adapter);


        builder.setView(dialogView);
        final AlertDialog dialog = builder.create();
        dialog.setTitle("Selecione uma imagem de perfil");
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();
        lvDialogImagens.setOnItemClickListener(new AdapterView.OnItemClickListener(){

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                    imgPerfilFilho.setBackground(ResourcesCompat.getDrawable(getResources(), array.get(position), null));
                    dialog.dismiss();
            }});


    }

    public  void  cadastarFilho(String email) {
        final AlertDialog.Builder dlgAlert  = new AlertDialog.Builder(FilhoCadastroActivity.this);
        dlgAlert.setTitle("LUDIT - Erro no Cadastro do Filho");
        dlgAlert.setPositiveButton("OK", null);
        dlgAlert.setCancelable(true);


        UserService service = RetrofitConfig.getClient().create(UserService.class);

        Filho filho = new Filho(dataDeNascimento, edtTexto.getText().toString(), "null", edtNome.getText().toString(), edtDeficiencia.getText().toString());
        if(edtTexto.getText().toString() == null || edtDeficiencia.getText().toString() == null || edtNome.getText().toString() == null || dataDeNascimento == null)
        {
            dlgAlert.setMessage("Preencha todos os Campos");
            dlgAlert.create().show();
        }else
        {
            Call<List<Filho>> call = service.inserirFilho(email, filho);

            call.enqueue(new Callback<List<Filho>>() {
                @Override
                public void onResponse(Call<List<Filho>> call, Response<List<Filho>> response) {
                    if(!response.isSuccessful()){

                        dlgAlert.setMessage("Erro ao Cadastrar, verifique os dados");
                        dlgAlert.create().show();
                        return;
                    }

                    SharedPreferences.Editor editor = getApplicationContext().getSharedPreferences("minhaShared", MODE_PRIVATE).edit();
                    editor.putString("nomeFilho", response.body().get(0).getNome());
                    editor.commit();

                    Intent i = new Intent(FilhoCadastroActivity.this, FilhoActivity.class);
                    startActivity(i);
                }

                @Override
                public void onFailure(Call<List<Filho>> call, Throwable t) {
                    Toast.makeText(getApplicationContext(), t.getMessage(), Toast.LENGTH_LONG ).show();
                }
            });
        }

    }

    @Override
    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.YEAR, year);
        c.set(Calendar.MONTH, month);
        c.set(Calendar.DAY_OF_MONTH, day);
        dataDeNascimento = DateFormat.getDateInstance().format(c.getTime());

        btnDataNascimento.setText(dataDeNascimento);
    }
}
