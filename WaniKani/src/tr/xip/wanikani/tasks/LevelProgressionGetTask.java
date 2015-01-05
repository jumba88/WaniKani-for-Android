package tr.xip.wanikani.tasks;

import android.content.Context;
import android.os.AsyncTask;

import tr.xip.wanikani.api.WaniKaniApi;
import tr.xip.wanikani.api.response.LevelProgression;
import tr.xip.wanikani.tasks.callbacks.LevelProgressionGetTaskCallbacks;

/**
 * Created by Hikari on 1/3/15.
 */
public class LevelProgressionGetTask extends AsyncTask<Void, Void, LevelProgression> {

    private Context context;

    private LevelProgressionGetTaskCallbacks mCallbacks;

    public LevelProgressionGetTask(Context context, LevelProgressionGetTaskCallbacks callbacks) {
        this.context = context;
        mCallbacks = callbacks;
    }

    public void executeSerial() {
        execute();
    }

    public void executeParallel() {
        executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        if (mCallbacks != null)
            mCallbacks.onLevelProgressionGetTaskPreExecute();
    }

    @Override
    protected LevelProgression doInBackground(Void... params) {
        try {
            return new WaniKaniApi(context).getLevelProgression();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void onPostExecute(LevelProgression progression) {
        super.onPostExecute(progression);
/*
        if (progression != null)
            // TODO: Save to database
        else
            progression = // TODO: Get from database
*/

        if (mCallbacks != null)
            mCallbacks.onLevelProgressionGetTaskPostExecute(progression);
    }
}