/*
 * Copyright 2014 Feng Dai
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.fragmentmaster.app;

import android.app.Activity;
import android.os.Build;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;

interface EventDispatcher {

    boolean dispatchKeyEvent(KeyEvent event);

    boolean dispatchKeyShortcutEvent(KeyEvent event);

    boolean dispatchTouchEvent(MotionEvent event);

    boolean dispatchTrackballEvent(MotionEvent event);

    boolean dispatchGenericMotionEvent(MotionEvent event);
}

/**
 * Event dispatch order:
 * <p/>
 * 1. Activity's Window <br/>
 * 2. Interceptor (If there is a Interceptor) <br/>
 * 3. Activity <br/>
 */
class MasterEventDispatcher implements EventDispatcher {

    private Activity mActivity;
    private EventDispatcher mEventInterceptor;
    private WindowEventDispatcher mWindowEventDispatcher;
    private ActivityEventDispatcher mActivityEventDispatcher;

    MasterEventDispatcher(Activity activity) {
        mActivity = activity;
        mWindowEventDispatcher = new WindowEventDispatcher(activity);
        mActivityEventDispatcher = new ActivityEventDispatcher(activity);
    }

    public void setInterceptor(EventDispatcher eventHandler) {
        this.mEventInterceptor = eventHandler;
    }

    public EventDispatcher getInterceptor() {
        return mEventInterceptor;
    }

    public boolean hasInterceptor() {
        return mEventInterceptor != null;
    }

    public boolean dispatchKeyEvent(KeyEvent event) {
        mActivity.onUserInteraction();
        return mWindowEventDispatcher.dispatchKeyEvent(event)
                || (hasInterceptor() && mEventInterceptor.dispatchKeyEvent(event))
                || mActivityEventDispatcher.dispatchKeyEvent(event);
    }

    public boolean dispatchKeyShortcutEvent(KeyEvent event) {
        mActivity.onUserInteraction();
        return mWindowEventDispatcher.dispatchKeyShortcutEvent(event)
                || (hasInterceptor() && mEventInterceptor.dispatchKeyShortcutEvent(event))
                || mActivityEventDispatcher.dispatchKeyShortcutEvent(event);
    }

    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            mActivity.onUserInteraction();
        }
        return mWindowEventDispatcher.dispatchTouchEvent(ev)
                || (hasInterceptor() && mEventInterceptor.dispatchTouchEvent(ev))
                || mActivityEventDispatcher.dispatchTouchEvent(ev);
    }

    public boolean dispatchTrackballEvent(MotionEvent ev) {
        mActivity.onUserInteraction();
        return mWindowEventDispatcher.dispatchTrackballEvent(ev)
                || (hasInterceptor() && mEventInterceptor.dispatchTrackballEvent(ev))
                || mActivityEventDispatcher.dispatchTrackballEvent(ev);
    }

    public boolean dispatchGenericMotionEvent(MotionEvent ev) {
        mActivity.onUserInteraction();
        return mWindowEventDispatcher.dispatchGenericMotionEvent(ev)
                || (hasInterceptor() && mEventInterceptor.dispatchGenericMotionEvent(ev))
                || mActivityEventDispatcher.dispatchGenericMotionEvent(ev);
    }
}

class WindowEventDispatcher implements EventDispatcher {

    private Activity mActivity;

    WindowEventDispatcher(Activity activity) {
        mActivity = activity;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        return mActivity.getWindow().superDispatchKeyEvent(event);
    }

    @Override
    public boolean dispatchKeyShortcutEvent(KeyEvent event) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            return mActivity.getWindow().superDispatchKeyShortcutEvent(event);
        }else{
            return false;
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        return mActivity.getWindow().superDispatchTouchEvent(event);
    }

    @Override
    public boolean dispatchTrackballEvent(MotionEvent event) {
        return mActivity.getWindow().superDispatchTrackballEvent(event);
    }

    @Override
    public boolean dispatchGenericMotionEvent(MotionEvent event) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
            return mActivity.getWindow().superDispatchGenericMotionEvent(event);
        }else{
            return false;
        }
    }
}

class ActivityEventDispatcher implements EventDispatcher {

    private Activity mActivity;

    ActivityEventDispatcher(Activity activity) {
        mActivity = activity;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        final View decor = mActivity.getWindow().getDecorView();
        return event.dispatch(mActivity, decor != null ? decor.getKeyDispatcherState() : null, mActivity);
    }

    @Override
    public boolean dispatchKeyShortcutEvent(KeyEvent event) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            return mActivity.onKeyShortcut(event.getKeyCode(), event);
        }else{
            return false;
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        return mActivity.onTouchEvent(event);
    }

    @Override
    public boolean dispatchTrackballEvent(MotionEvent event) {
        return mActivity.onTrackballEvent(event);
    }

    @Override
    public boolean dispatchGenericMotionEvent(MotionEvent event) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
            return mActivity.onGenericMotionEvent(event);
        }else{
            return false;
        }
    }
}

class FragmentEventDispatcher implements EventDispatcher {

    private IMasterFragment mMasterFragment;

    FragmentEventDispatcher(IMasterFragment masterFragment) {
        mMasterFragment = masterFragment;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        View view = mMasterFragment.getView();
        return event.dispatch(mMasterFragment, view != null ? view.getKeyDispatcherState() : null, this);
    }

    @Override
    public boolean dispatchKeyShortcutEvent(KeyEvent event) {
        return mMasterFragment.onKeyShortcut(event.getKeyCode(), event);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        return mMasterFragment.onTouchEvent(event);
    }

    @Override
    public boolean dispatchTrackballEvent(MotionEvent event) {
        return mMasterFragment.onTrackballEvent(event);
    }

    @Override
    public boolean dispatchGenericMotionEvent(MotionEvent event) {
        return mMasterFragment.onGenericMotionEvent(event);
    }
}