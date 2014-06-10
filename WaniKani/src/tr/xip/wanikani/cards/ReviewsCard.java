package tr.xip.wanikani.cards;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.joda.time.Period;
import org.joda.time.format.PeriodFormatter;

import java.text.SimpleDateFormat;

import tr.xip.wanikani.BroadcastIntents;
import tr.xip.wanikani.DashboardFragment;
import tr.xip.wanikani.R;
import tr.xip.wanikani.api.WaniKaniApi;
import tr.xip.wanikani.api.response.StudyQueue;
import tr.xip.wanikani.api.response.User;
import tr.xip.wanikani.managers.OfflineDataManager;
import tr.xip.wanikani.managers.ThemeManager;
import tr.xip.wanikani.utils.Utils;

/**
 * Created by xihsa_000 on 3/13/14.
 */
public class ReviewsCard extends Fragment {

    Context context;

    WaniKaniApi api;
    OfflineDataManager dataMan;
    Utils utils;
    ThemeManager themeMan;

    View rootView;

    ReviewsCardListener mListener;

    TextView mNextReview;
    TextView mNextHour;
    TextView mNextDay;

    LinearLayout mCard;

    Period period;
    PeriodFormatter formatter;

    long nextReview;
    int nextHour;
    int nextDay;
    boolean isVacationModeActive;
    int reviewsAvailable;

    public ReviewsCard(ReviewsCardListener listener, Context context) {
        mListener = listener;
        LocalBroadcastManager.getInstance(context).registerReceiver(mDoLoad,
                new IntentFilter(BroadcastIntents.SYNC()));
    }

    private BroadcastReceiver mDoLoad = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Build.VERSION.SDK_INT >= 11)
                new LoadTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            else
                new LoadTask().execute();
        }
    };

    @Override
    public void onCreate(Bundle state) {
        api = new WaniKaniApi(getActivity());
        dataMan = new OfflineDataManager(getActivity());
        utils = new Utils(getActivity());
        themeMan = new ThemeManager(getActivity());
        super.onCreate(state);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.card_reviews, null);

        context = getActivity();

        mNextReview = (TextView) rootView.findViewById(R.id.card_reviews_next_review);
        mNextHour = (TextView) rootView.findViewById(R.id.card_reviews_next_hour);
        mNextDay = (TextView) rootView.findViewById(R.id.card_reviews_next_day);

        mCard = (LinearLayout) rootView.findViewById(R.id.card_reviews_card);
        mCard.setBackgroundResource(themeMan.getCard());

        loadOfflineValues();

        return rootView;
    }

    private void loadOfflineValues() {
        mNextHour.setText(dataMan.getReviewsAvailableNextHour() + "");
        mNextDay.setText(dataMan.getReviewsAvailableNextDay() + "");

        if (dataMan.getReviewsAvailable() != 0) {
            mNextReview.setText(getString(R.string.card_content_reviews_available_now));
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM HH:mm");
            mNextReview.setText(sdf.format(dataMan.getNextReviewDate()));
        }
    }

    private void saveOfflineValues() {
        dataMan.setNextReviewDate(nextReview);
        dataMan.setReviewsAvailableNextHour(nextHour);
        dataMan.setReviewsAvailableNextDay(nextDay);
    }

    private class LoadTask extends AsyncTask<String, Void, String> {
        StudyQueue studyQueue;

        @Override
        protected String doInBackground(String... strings) {
            try {
                studyQueue = api.getStudyQueue();
                nextReview = studyQueue.getNextReviewDate();
                nextHour = studyQueue.getAvailableReviewsNextHourCount();
                nextDay = studyQueue.getAvailableReviewsNextDayCount();
                reviewsAvailable = studyQueue.getAvailableReviewsCount();
                isVacationModeActive = api.isVacationModeActive();
                return "success";
            } catch (Exception e) {
                e.printStackTrace();
                return "failure";
            }
        }

        @Override
        protected void onPostExecute(String result) {
            if (!isVacationModeActive) {
                if (result.equals("success")) {
                    mNextHour.setText(nextHour + "");
                    mNextDay.setText(nextDay + "");

                    if (reviewsAvailable != 0) {
                        mNextReview.setText(R.string.card_content_reviews_available_now);
                    } else {
                        SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM HH:mm");
                        mNextReview.setText(sdf.format(nextReview));
                    }

                    mListener.onReviewsCardSyncFinishedListener(DashboardFragment.SYNC_RESULT_SUCCESS);

                    saveOfflineValues();
                } else {
                    mListener.onReviewsCardSyncFinishedListener(DashboardFragment.SYNC_RESULT_FAILED);
                }
            } else {
                // TODO - Vacation mode
            }
        }
    }

    public interface ReviewsCardListener {
        public void onReviewsCardSyncFinishedListener(String result);
    }
}
