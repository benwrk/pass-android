package co.bwsc.pass.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import co.bwsc.pass.R;

/**
 * Created by Ben on 5/2/2017.
 */

public class PopupDialogActivity extends AppCompatActivity {

    public static final String TEXT_EXTRA = "TEXT_EXTRA";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
//        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_popup_dialog);
        setTitle(getString(R.string.message));

        TextView messageView = (TextView) findViewById(R.id.popup_text);
        messageView.setText(getIntent().getStringExtra(TEXT_EXTRA));
        Button closeButton = (Button) findViewById(R.id.button_close);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
