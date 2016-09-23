/**
 *  Copyright 2014-2016 CyberVision, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.kaaproject.kaa.demo.cellmonitor;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.InputType;
import android.widget.EditText;

import org.kaaproject.kaa.client.profile.ProfileContainer;
import org.kaaproject.kaa.demo.cellmonitor.profile.CellMonitorProfile;





/**
 * The implementation of {@link ActionBarActivity} class. Notifies the application of the activity lifecycle changes.
 */
public class CellMonitorActivity extends ActionBarActivity {
/*
    private String m_Text = "";
    private CellMonitorApplication mApplication;
*/







    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cell_monitor);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new CellMonitorFragment()).commit();
        }

/*
        //added by komeil
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Authentication");
        // Set up the input
        final EditText input = new EditText(this);
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                m_Text = input.getText().toString();
                getCellMonitorApplication().updateProfilePassword(m_Text);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();*/


    }


    @Override
    protected void onPause() {
        super.onPause();

        /*
         * Notify the application of the background state.
         */

        getCellMonitorApplication().pause();
    }

    @Override
    protected void onResume() {
        super.onResume();

        /*
         * Notify the application of the foreground state.
         */

        getCellMonitorApplication().resume();
    }
    
    public CellMonitorApplication getCellMonitorApplication() {
        return (CellMonitorApplication) getApplication();
    }
}
