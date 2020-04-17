package com.asd412id.bukutamu.ui.history;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class HistoryViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public HistoryViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("Masih dalam tahap pengembangan\n@_@");
    }

    public LiveData<String> getText() {
        return mText;
    }
}