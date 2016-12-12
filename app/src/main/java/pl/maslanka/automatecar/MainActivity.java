package pl.maslanka.automatecar;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.admin.DevicePolicyManager;
import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import pl.maslanka.automatecar.utils.DeviceAdminLock;
import pl.maslanka.automatecar.helpers.ActivityForResult;
import pl.maslanka.automatecar.prefconnected.PrefsCarConnected;
import pl.maslanka.automatecar.helpers.Constants;
import pl.maslanka.automatecar.services.MainService;
import pl.maslanka.automatecar.utils.Logic;

public class MainActivity extends AppCompatActivity implements View.OnClickListener,
        CompoundButton.OnCheckedChangeListener, Constants.DEFAULT_VALUES, Constants.PREF_KEYS {

    private final String LOG_TAG = this.getClass().getSimpleName();
    private static final int DEVICE_ADMIN_REQUEST_CODE = 1346;
    private static final int ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE = 2541;
    private static final int ACCESSIBILITY_MANAGER_REQUEST_CODE = 3462;
    private static final String ADMIN_DIALOG_WAS_SHOWING = "admin_dialog_was_showing";
    private static final String OVERLAY_DIALOG_WAS_SHOWING = "overlay_dialog_was_showing";
    private static final String ACCESSIBILITY_DIALOG_WAS_SHOWING = "accessibility_dialog_was_showing";

    private Switch automateServiceRunning;
    private CardView carConnected;
    private CardView carDisconnected;
    private CardView inCarOptions;
    private CardView otherOptions;

    private boolean firstRun;
    private boolean adminDialogWasShowing;
    private boolean overlayDialogWasShowing;
    private boolean accessibilityDialogWasShowing;
    private boolean mainServiceWasRunning;

    private AlertDialog builderAdmin;
    private AlertDialog builderOverlay;
    private AlertDialog builderAccessibility;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState != null) {
            adminDialogWasShowing = savedInstanceState.getBoolean(ADMIN_DIALOG_WAS_SHOWING);
            overlayDialogWasShowing = savedInstanceState.getBoolean(OVERLAY_DIALOG_WAS_SHOWING);
            accessibilityDialogWasShowing = savedInstanceState.getBoolean(ACCESSIBILITY_DIALOG_WAS_SHOWING);
            Log.d(LOG_TAG, "firstAdminDialog: " + Boolean.toString(adminDialogWasShowing));
            Log.d(LOG_TAG, "firstOverlayDialog: " + Boolean.toString(overlayDialogWasShowing));
            Log.d(LOG_TAG, "firstAccessibilityDialog: " + Boolean.toString(accessibilityDialogWasShowing));
        }

        firstRun = Logic.getSharedPrefBoolean(this, KEY_FIRST_RUN, FIRST_RUN_DEFAULT_VALUE);

        findViews();

        if (BluetoothAdapter.getDefaultAdapter() != null) {
            setListeners();
            mainServiceWasRunning = Logic.isMyServiceRunning(MainService.class, getApplicationContext());
            automateServiceRunning.setChecked(mainServiceWasRunning);
        } else {
            showBluetoothNotSupportedDialog();
        }

        if (firstRun) {
            if (Logic.testDeviceAdminPermission(this)) {
               if (Logic.testSystemOverlayPermission(this)) {
                   if (!Logic.testAccessibilityPermission(this)) {
                       showAccessibilityServiceDialog();
                   }
               } else {
                   showSystemOverlayPermissionDialog();
               }
            } else {
                showDeviceAdminPermissionDialog();
            }
            Logic.setSharedPrefBoolean(this, false, KEY_FIRST_RUN);
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

    protected void showBluetoothNotSupportedDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getResources().getString(R.string.device_not_supported));
        builder.setMessage(getResources().getString(R.string.device_not_supported_message));
        builder.setPositiveButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        builder.create();
        builder.show();
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.car_connected:
                Log.d(LOG_TAG, "car connected card clicked");
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

        if (isChecked) {
            if (!mainServiceWasRunning) {
                if (!BluetoothAdapter.getDefaultAdapter().isEnabled()) {
                    android.content.Intent enableIntent = new android.content.Intent(
                            android.bluetooth.BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivity(enableIntent);
                }

                if (Logic.testDeviceAdminPermission(this)) {
                    if (Logic.testSystemOverlayPermission(this)) {
                        if (Logic.testAccessibilityPermission(this)) {
                            Intent startIntent = new Intent(MainActivity.this, MainService.class);
                            startIntent.setAction(Constants.ACTION.START_FOREGROUND_ACTION);
                            startService(startIntent);
                            Toast.makeText(this, getString(R.string.service_started), Toast.LENGTH_SHORT).show();
                        } else {
                            showAccessibilityServiceDialog();
                            buttonView.setChecked(false);
                        }
                    } else {
                        showSystemOverlayPermissionDialog();
                        buttonView.setChecked(false);
                    }
                } else {
                    showDeviceAdminPermissionDialog();
                    buttonView.setChecked(false);
                }

            }

        } else {
            if (Logic.isMyServiceRunning(MainService.class, getApplicationContext())) {
                Intent stopIntent = new Intent(MainActivity.this, MainService.class);
                stopIntent.setAction(Constants.ACTION.STOP_FOREGROUND_ACTION);
                startService(stopIntent);
                Toast.makeText(this, getString(R.string.service_stopped), Toast.LENGTH_SHORT).show();
                mainServiceWasRunning = false;
            }
        }

    }

    @SuppressLint("NewApi")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == DEVICE_ADMIN_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, getString(R.string.device_admin_permissions_granted), Toast.LENGTH_LONG).show();
            }

            if (!Logic.testSystemOverlayPermission(this)) {
                showSystemOverlayPermissionDialog();
            } else if (!Logic.testAccessibilityPermission(this)) {
                showAccessibilityServiceDialog();
            }
        } else if (requestCode == ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE) {
            if (Settings.canDrawOverlays(this))
                Toast.makeText(this, getString(R.string.force_rotation_permissions_granted), Toast.LENGTH_LONG).show();

            if (!Logic.testAccessibilityPermission(this))
                showAccessibilityServiceDialog();

        } else if (requestCode == ACCESSIBILITY_MANAGER_REQUEST_CODE) {
            if (Logic.testAccessibilityPermission(this)) {
                Toast.makeText(this, getString(R.string.accessibility_service_enabled), Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (builderAdmin != null) {
            adminDialogWasShowing = builderAdmin.isShowing();
            Log.d(LOG_TAG, "adminDialogWasShowing: " + Boolean.toString(adminDialogWasShowing));
            if (adminDialogWasShowing) {
                builderAdmin.dismiss();
            }
        }

        if (builderOverlay != null) {
            overlayDialogWasShowing = builderOverlay.isShowing();
            Log.d(LOG_TAG, "overlayDialogWasShowing: " + Boolean.toString(overlayDialogWasShowing));
            if (overlayDialogWasShowing) {
                builderOverlay.dismiss();
            }
        }

        if (builderAccessibility != null) {
            accessibilityDialogWasShowing = builderAccessibility.isShowing();
            Log.d(LOG_TAG, "accessibilityDialogWasShowing: " + Boolean.toString(accessibilityDialogWasShowing));
            if (accessibilityDialogWasShowing) {
                builderAccessibility.dismiss();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(LOG_TAG, "adminDialogIsShowing: " + Boolean.toString(adminDialogWasShowing));
        if (adminDialogWasShowing) {
            showDeviceAdminPermissionDialog();
        }

        Log.d(LOG_TAG, "overlayDialogIsShowing: " + Boolean.toString(overlayDialogWasShowing));

        if (overlayDialogWasShowing) {
            showSystemOverlayPermissionDialog();
        }

        Log.d(LOG_TAG, "accessibilityDialogIsShowing: " + Boolean.toString(accessibilityDialogWasShowing));

        if (accessibilityDialogWasShowing) {
            showAccessibilityServiceDialog();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean(ADMIN_DIALOG_WAS_SHOWING, adminDialogWasShowing);
        savedInstanceState.putBoolean(OVERLAY_DIALOG_WAS_SHOWING, overlayDialogWasShowing);
        savedInstanceState.putBoolean(ACCESSIBILITY_DIALOG_WAS_SHOWING, accessibilityDialogWasShowing);
        super.onSaveInstanceState(savedInstanceState);
    }

    protected void showDeviceAdminPermissionDialog() {
        final ComponentName compName = new ComponentName(this, DeviceAdminLock.class);

        builderAdmin = new AlertDialog.Builder(this)
                .setTitle(getString(R.string.admin_rights_needed))
                .setMessage(getString(R.string.need_device_admin_rights_dialog) + "\n\n" + getString(R.string.please_turn_on_admin_rights))
                .setPositiveButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
                        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, compName);
                        intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, getString(R.string.need_device_admin_rights_dialog));
                        startActivityForResult(intent, DEVICE_ADMIN_REQUEST_CODE);
                    }
                })
                .setNegativeButton(getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(MainActivity.this, ActivityForResult.class);
                        intent.putExtra(Constants.INTENT_EXTRA_RESULT_CODE, Activity.RESULT_CANCELED);
                        startActivityForResult(intent, DEVICE_ADMIN_REQUEST_CODE);
                    }
                })
                .setCancelable(false)
                .create();

        builderAdmin.show();
    }

    @SuppressLint("NewApi")
    protected void showSystemOverlayPermissionDialog() {
        builderOverlay = new AlertDialog.Builder(this)
                .setTitle(getString(R.string.force_rotation_rights_needed))
                .setMessage(getString(R.string.force_rotation_rights_dialog) + "\n\n" + getString(R.string.please_turn_on_system_overlay_rights))
                .setPositiveButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                Uri.parse("package:" + getPackageName()));
                        startActivityForResult(intent, ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE);
                    }
                })
                .setNegativeButton(getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(MainActivity.this, ActivityForResult.class);
                        intent.putExtra(Constants.INTENT_EXTRA_RESULT_CODE, Activity.RESULT_CANCELED);
                        startActivityForResult(intent, ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE);
                    }
                })
                .setCancelable(false)
                .create();

        builderOverlay.show();
    }

    protected void showAccessibilityServiceDialog() {
        builderAccessibility = new AlertDialog.Builder(this)
                .setTitle(getString(R.string.accessibility_service_needed))
                .setMessage(getString(R.string.accessibility_service_dialog) + "\n\n" + getString(R.string.please_turn_on_accessibility_service))
                .setPositiveButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startActivityForResult(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS), ACCESSIBILITY_MANAGER_REQUEST_CODE);
                    }
                })
                .setNegativeButton(getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(MainActivity.this, ActivityForResult.class);
                        intent.putExtra(Constants.INTENT_EXTRA_RESULT_CODE, Activity.RESULT_CANCELED);
                        startActivityForResult(intent, ACCESSIBILITY_MANAGER_REQUEST_CODE);
                    }
                })
                .setCancelable(false)
                .create();

        builderAccessibility.show();
    }

}





