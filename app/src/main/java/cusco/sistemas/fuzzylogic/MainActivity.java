package cusco.sistemas.fuzzylogic;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;

import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import android.widget.TextView;
import android.widget.Toast;

import net.sourceforge.jFuzzyLogic.FIS;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;



public class MainActivity extends AppCompatActivity implements View.OnClickListener {


    private Button btnListar;
    private Button btnConectar;
    private Button btnLeer;



    private Button arriba;
    private Button abajo;
    private Button derecha;
    private Button izquierda;
    private Button offMotor;


    private ListView lstListar;
    private EditText txtMac;


    private TextView distancia;
    private TextView alcohol;


    private ProgressDialog progressDialog;
    private boolean isConnect;

    private final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");


    private BluetoothAdapter bluetoothAdapter;
    private Set<BluetoothDevice> paiBluetoothDevices;
    private BluetoothSocket BtSocket;



    private DataOutputStream dou;
    private DataInputStream diu;

    private boolean lectura;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(bluetoothAdapter == null){
            Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            if(bluetoothAdapter.isEnabled()){
                Toast.makeText(this, "Bluetooth Activado", Toast.LENGTH_SHORT).show();
            } else {
                Intent i = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(i, 1);
            }
        }

        setContentView(R.layout.activity_main);
        distancia = (TextView) findViewById(R.id.tvDistancia);
        alcohol = (TextView) findViewById(R.id.tvAlcohol);
        lstListar = (ListView) findViewById(R.id.tvLista);
        txtMac = (EditText) findViewById(R.id.txtMacAddress);
        txtMac.setInputType(InputType.TYPE_NULL);

        arriba = (Button) findViewById(R.id.btnArriba);
        abajo = (Button) findViewById(R.id.btnAbajo);
        derecha = (Button) findViewById(R.id.btnDerecha);
        izquierda = (Button) findViewById(R.id.btnIzquierda);
        offMotor = (Button) findViewById(R.id.btnOff);

        btnLeer = (Button) findViewById(R.id.btnLeer);
        btnConectar = (Button) findViewById(R.id.btnConectar);
        btnListar = (Button) findViewById(R.id.btnListar);
        btnListar.setOnClickListener(this);
        btnConectar.setOnClickListener(this);
        btnLeer.setOnClickListener(this);
        arriba.setOnClickListener(this);
        abajo.setOnClickListener(this);
        derecha.setOnClickListener(this);
        izquierda.setOnClickListener(this);
        offMotor.setOnClickListener(this);

        isConnect = false;



    }

    @Override
    public void onClick(View view) {

        if(view.getId() == R.id.btnConectar){
            conectar();
            lectura = true;
        }
        if(view.getId() == R.id.btnListar){
            dispositivosEnparejados();

        }

        if(view.getId() == R.id.btnLeer){
            leer();
            String nivel = alcohol.getText().toString();
            String dista = distancia.getText().toString();
            ControlDifuzo c = new ControlDifuzo();
            c.defuzificacion(nivel, dista);
        }

        if(view.getId() == R.id.btnArriba){
            try {
                dou.write((int) 'a');
            } catch (IOException e){
                e.printStackTrace();
            }
        }
        if(view.getId() == R.id.btnAbajo){
            try {
                dou.write((int) 'b');
            } catch (IOException e){
                e.printStackTrace();
            }
        }
        if(view.getId() == R.id.btnDerecha){
            try {
                dou.write((int) 'c');
            } catch (IOException e){
                e.printStackTrace();
            }
        }
        if(view.getId() == R.id.btnIzquierda){
            try {
                dou.write((int) 'd');
            } catch (IOException e){
                e.printStackTrace();
            }
        }
        if(view.getId() == R.id.btnOff){
            try {
                dou.write((int) 'e');
            } catch (IOException e){
                e.printStackTrace();
            }
        }

    }

    private void dispositivosEnparejados(){
        paiBluetoothDevices = bluetoothAdapter.getBondedDevices();
        List<String> list = new ArrayList<>();
        if(paiBluetoothDevices.size() > 0){
            for (BluetoothDevice bt : paiBluetoothDevices){
                list.add(bt.getName()+"\n"+bt.getAddress());
            }
        } else {
            Toast.makeText(this, "No existen dispositivos vinculados", Toast.LENGTH_SHORT).show();
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, list);
        lstListar.setAdapter(adapter);
    }

    private void conectar(){
        new ConexionBT() .execute();
    }

    private void leer(){
        try {
            distancia.setText(""+diu.read());
            alcohol.setText(""+diu.read());
        } catch (Exception e){
            Toast.makeText(this, "Error de lectura", Toast.LENGTH_SHORT).show();
        }
    }


    private class ConexionBT extends AsyncTask<Void, Void, Void>{

        private boolean connectSuccess = true;

        @Override
        protected void onPreExecute() {
            //super.onPreExecute();
            progressDialog = ProgressDialog.show(MainActivity.this, "Intentando Conectar...", "Espera!!!");  //show a progress dialog
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                if(BtSocket == null || !isConnect){
                    bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                    BluetoothDevice btD = bluetoothAdapter.getRemoteDevice(txtMac.getText().toString());
                    BtSocket = btD.createInsecureRfcommSocketToServiceRecord(myUUID);
                    bluetoothAdapter.cancelDiscovery();
                    BtSocket.connect();

                    dou = new DataOutputStream(BtSocket.getOutputStream());
                    diu = new DataInputStream(BtSocket.getInputStream());

                }
            } catch (Exception e){
                connectSuccess = false;
                Log.e("Error: " ,e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if(!connectSuccess){
                Toast.makeText(getApplicationContext(),"No se pudo conectar!!!",Toast.LENGTH_SHORT).show();
                try {

                }catch(Exception e){
                    Log.e("Error: ", e.getMessage());
                }
            }else{
                isConnect = true;
                Toast.makeText(getApplicationContext(),"Ahora estas conectado!!!",Toast.LENGTH_SHORT).show();
            }
            progressDialog.dismiss();
        }

    }

    private class ControlDifuzo{
        FIS fis;
        public ControlDifuzo(){
            try {
                fis = FIS.load(getApplicationContext().getResources().getAssets().open("Control", Context.MODE_WORLD_READABLE), true);
            } catch (IOException e){
                e.printStackTrace();
            }
        }
        public void defuzificacion(String a, String d){
            //fis.setVariable("alcohol", Double.parseDouble(a));
            //fis.setVariable("distancia", Double.parseDouble(d));
            fis.setVariable("alcohol", 40);
            fis.setVariable("distancia", 80);
            fis.evaluate();
            double resultado = fis.getVariable("velocidad").getLatestDefuzzifiedValue();
            Toast.makeText(MainActivity.this, "VELOCIDAD SUGERIDA: "+resultado, Toast.LENGTH_SHORT).show();
        }
    }
}
