package ca.ualberta.cs.cmput301f14t14.questionapp.data.threading;

import ca.ualberta.cs.cmput301f14t14.questionapp.data.DataManager;
import ca.ualberta.cs.cmput301f14t14.questionapp.data.IDataStore;
import ca.ualberta.cs.cmput301f14t14.questionapp.data.eventbus.EventBus;
import ca.ualberta.cs.cmput301f14t14.questionapp.data.eventbus.events.QuestionPushDelayedEvent;
import ca.ualberta.cs.cmput301f14t14.questionapp.model.Question;
import android.content.Context;

public class AddQuestionTask extends AbstractDataManagerTask<Question, Void, Boolean>{

	public AddQuestionTask(Context c) {
		super(c);
	}

	@Override
	protected Boolean doInBackground(Question... qin) {
		Question q = qin[0]; // Ignore other questions inputted
		IDataStore remote = DataManager.getInstance(this.getContext())
			.getRemoteDataStore();
		
		if (remote.hasAccess()){
			remote.putQuestion(q);
			remote.save();
			return true;
		} else {
			//Put into local data store and don't mark the task as complete
			IDataStore local = DataManager.getInstance(getContext()).getLocalDataStore();
			if (local.getQuestion(q.getId()) != null) {
				//Don't put the same question into the store twice
				local.putQuestion(q);
				local.save();
			}
			EventBus.getInstance().addEvent(new QuestionPushDelayedEvent(q));
			return false;
		}
		
	}
	
	@Override
	protected void onPostExecute(Boolean finished) {
		if (callback == null) {
			return;
		}
		callback.run(finished);
	}

}
