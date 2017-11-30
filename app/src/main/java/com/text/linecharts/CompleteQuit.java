package com.text.linecharts;

import android.app.Activity;
import android.app.Application;

import java.util.Stack;

/**
 * 完全退出帮助类
 * 
 */
public class CompleteQuit extends Application {

	private Stack<Activity> activityStack;
	private static CompleteQuit instance;

	private CompleteQuit() {
	}

	/**
	 * 单例模式中获取唯一的CompleteQuit实例
 	 */
	public static CompleteQuit getInstance() {
		if (null == instance) {
			instance = new CompleteQuit();
		}
		return instance;

	}

	/**
	 * 添加Activity到容器中
 	 */

	public void pushActivity(Activity activity) {
		if (activityStack == null) {
			activityStack = new Stack<Activity>();
		}
		activityStack.add(activity);
	}

	/**
	 * 退出栈中所有Activity(唯一列外)
 	 */

	@SuppressWarnings("rawtypes")
	public void exitAllButOne(Class cls) {
		while (true) {
			Activity activity = currentActivity();
			if (activity == null) {
				break;
			}
			if (activity.getClass().equals(cls)) {
				break;
			}
			popActivity(activity);
		}
	}

	/**
	 * 退出栈中所有Activity
 	 */
	public void exitAll(boolean exit) {
		while (true) {
			Activity activity = currentActivity();
			if (activity == null) {
				break;
			}
			popActivity(activity);
		}
		if (exit) {
			System.exit(0);
		}
	}

	/**
	 * 获得当前栈顶Activity
	 */
	public Activity currentActivity() {
		Activity activity = null;
		if (activityStack != null && !activityStack.empty())
			activity = activityStack.lastElement();
		return activity;
	}

	/**
	 * 退出栈顶Activity
	 */
	public void popActivity(Activity activity) {
		if (activity != null) {
			// 在从自定义集合中取出当前Activity时，也进行了Activity的关闭操作
			activity.finish();
			activityStack.remove(activity);
			activity = null;
		}
	}
}