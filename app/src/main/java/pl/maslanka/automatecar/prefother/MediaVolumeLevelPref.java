package pl.maslanka.automatecar.prefother;

import android.content.Context;
import android.preference.Preference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import pl.maslanka.automatecar.R;
import pl.maslanka.automatecar.helpers.Constants;
import pl.maslanka.automatecar.utils.Logic;

/**
 * Created by Artur on 18.12.2016.
 */

public class MediaVolumeLevelPref extends Preference implements Constants.PREF_KEYS,
        Constants.DEFAULT_VALUES {

    private final String LOG_TAG = this.getClass().getSimpleName();

    private SeekBar mediaVolumeSeekBar;
    private TextView currentLevel;
    private int progressToSet;
    private int newProgress;


    public MediaVolumeLevelPref(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    @Override
    protected View onCreateView(ViewGroup parent) {
        return super.onCreateView(parent);
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        mediaVolumeSeekBar = (SeekBar) view.findViewById(R.id.media_volume);
        currentLevel = (TextView) view.findViewById(R.id.current_level);

        progressToSet = Integer.parseInt(Logic.getSharedPrefString(getContext(),
                KEY_MEDIA_VOLUME_LEVEL_IN_CAR, Integer.toString(MEDIA_VOLUME_LEVEL_IN_CAR_DEFAULT_VALUE)));

        mediaVolumeSeekBar.setProgress(progressToSet);
        currentLevel.setText(String.format(getContext().getString(R.string.current_level), progressToSet));

        mediaVolumeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                newProgress = progress;
                currentLevel.setText(String.format(getContext().getString(R.string.current_level), newProgress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Logic.setSharedPrefString(getContext(), Integer.toString(newProgress), KEY_MEDIA_VOLUME_LEVEL_IN_CAR);
                Log.v(LOG_TAG, "New media volume progress set: " + Integer.toString(newProgress));
            }
        });

    }
}
