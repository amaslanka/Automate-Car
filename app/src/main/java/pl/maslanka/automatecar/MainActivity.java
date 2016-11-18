package pl.maslanka.automatecar;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import pl.maslanka.automatecar.connectedpref.PrefsCarConnected;
import pl.maslanka.automatecar.helperobjectsandinterfaces.Constants;
import pl.maslanka.automatecar.utils.Logic;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    private static Logic logic;
    private Switch automateServiceRunning;
    private CardView carConnected;
    private CardView carDisconnected;
    private CardView inCarOptions;
    private CardView otherOptions;

    public static Logic getLogic() {
        return logic;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        logic = new Logic();

        findViews();

        if (logic.getMBluetoothAdapter() != null) {
            setListeners();
        } else {
            showAlertDialog();
        }

    }

    protected void findViews() {
        automateServiceRunning = (Switch) findViewById(R.id.automate_service_running);
        carConnected = (CardView) findViewById(R.id.car_connected);
        carDisconnected = (CardView) findViewById(R.id.car_disconnected);
        inCarOptions = (CardView) findViewById(R.id.in_car_options);
        otherOptions = (CardView) findViewById(R.id.other_options);
    }

    protected void setListeners() {
        automateServiceRunning.setOnCheckedChangeListener(this);
        carConnected.setOnClickListener(this);
        carDisconnected.setOnClickListener(this);
        inCarOptions.setOnClickListener(this);
        otherOptions.setOnClickListener(this);
    }

    protected void showAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getResources().getString(R.string.device_not_supported));
        builder.setMessage(getResources().getString(R.string.device_not_supported_message));
        builder.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.create();
        builder.show();
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.car_connected:
                Log.d("car connected", "card clicked");
                Intent carConnectedIntent = new Intent(MainActivity.this, PrefsCarConnected.class);
                startActivity(carConnectedIntent);
                break;
            case R.id.car_disconnected:

                break;

            case R.id.in_car_options:

                break;

            case R.id.other_options:

                break;

            default:
                break;
        }

    }


    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        Toast.makeText(this, (isChecked ? getString(R.string.service_started): getString(R.string.service_stopped)),
                Toast.LENGTH_SHORT).show();

        if (isChecked) {
            Intent startIntent = new Intent(MainActivity.this, MainService.class);
            startIntent.setAction(Constants.ACTION.STARTFOREGROUND_ACTION);
            startService(startIntent);
        } else {
            Intent stopIntent = new Intent(MainActivity.this, MainService.class);
            stopIntent.setAction(Constants.ACTION.STOPFOREGROUND_ACTION);
            startService(stopIntent);
        }

    }

}

