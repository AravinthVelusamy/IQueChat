package net.iquesoft.android.seedprojectchat.presenter.classes.fragments;

import android.util.Log;

import com.arellomobile.mvp.InjectViewState;
import com.arellomobile.mvp.MvpPresenter;
import com.backendless.Backendless;
import com.backendless.BackendlessCollection;
import com.backendless.BackendlessUser;
import com.backendless.async.callback.BackendlessCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.persistence.BackendlessDataQuery;

import net.iquesoft.android.seedprojectchat.common.DefaultBackendlessCallback;
import net.iquesoft.android.seedprojectchat.model.Friends;
import net.iquesoft.android.seedprojectchat.presenter.interfaces.fragments.IFindFriendFragmentPresenter;
import net.iquesoft.android.seedprojectchat.view.interfaces.fragments.IFindFriendFragment;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

@InjectViewState
public class FindFriendFragmentPresenter extends MvpPresenter<IFindFriendFragment> implements IFindFriendFragmentPresenter {

    private PublishSubject<List<BackendlessUser>> usersObservable = PublishSubject.create();
    private static PublishSubject<List<Friends>> friendsObservable = PublishSubject.create();
    private String whereClause = "(user_one.objectId='" + Backendless.UserService.CurrentUser().getProperty("objectId") + "'" +
            " or " + "user_two.objectId='" + Backendless.UserService.CurrentUser().getProperty("objectId") + "')" +
            " and " + "status = '2'";
    private BackendlessDataQuery dataQuery = new BackendlessDataQuery(whereClause);
    private Subscription subscription;
    private String username = "";

    public void setUsername(String username) {
        this.username = username;
    }

    public synchronized PublishSubject<List<Friends>> getCurentFriendList() {
        friendsObservable.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
        Friends.findAsync(dataQuery, new DefaultBackendlessCallback<BackendlessCollection<Friends>>() {
            @Override
            public void handleResponse(BackendlessCollection<Friends> response) {
                friendsObservable.onNext(response.getData());
            }
        });
        return friendsObservable;
    }

    public synchronized void updateCurentFriendList(){

        Friends.findAsync(dataQuery, new DefaultBackendlessCallback<BackendlessCollection<Friends>>() {
            @Override
            public void handleResponse(BackendlessCollection<Friends> response) {
                friendsObservable.onNext(response.getData());
            }
        });
    }

    @Override
    public void attachView(IFindFriendFragment view) {
        getViewState().setProgressBarVisible();
        subscription = getCurentFriendList().subscribe(response ->{
            getBackendlessUsers(username, response).observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io()).subscribe(respons -> getViewState().setUserAdapter(respons));
        });
        super.attachView(view);
    }

    @Override
    public void detachView(IFindFriendFragment view) {
        subscription.unsubscribe();
        super.detachView(view);
    }

    @Override
    public PublishSubject<List<BackendlessUser>> getBackendlessUsers(String username, List<Friends> friendsList) {
        String usernamequery = "name LIKE" +
                "'%" + username + "%'" +
                " or " +
                "email LIKE" +
                "'%" + username + "%'";
        BackendlessDataQuery dataQuery = new BackendlessDataQuery();
        dataQuery.setWhereClause(usernamequery);
            Backendless.Data.of(BackendlessUser.class).find(dataQuery, new BackendlessCallback<BackendlessCollection<BackendlessUser>>() {
                @Override
                public void handleResponse(BackendlessCollection<BackendlessUser> response) {
                    usersObservable.onNext(response.getData());
                }

                @Override
                public void handleFault(BackendlessFault fault) {
                    super.handleFault(fault);
                }
            });


        return checkBackendlessUser(friendsList);
    }

    private PublishSubject<List<BackendlessUser>> checkBackendlessUser(List<Friends> friendsList){
        PublishSubject<List<BackendlessUser>> usersObs = PublishSubject.create();
        ArrayList<BackendlessUser> users = new ArrayList<>();
        if (friendsList.size() != 0){
            usersObservable.flatMap(Observable::from)
                    .filter(response -> !response.getObjectId().equals(Backendless.UserService.CurrentUser().getProperty("objectId")))
                    .skipWhile(response -> check(response, friendsList))
                    .subscribe(user->{
                        Log.i("user", user.toString());
                        users.add(user);
                        usersObs.onNext(users);
                    });
        } else {
            usersObservable.flatMap(Observable::from)
                    .filter(response -> !response.getObjectId().equals(Backendless.UserService.CurrentUser().getProperty("objectId")))
                    .subscribe(user->{
                        Log.i("user", user.toString());
                        users.add(user);
                        usersObs.onNext(users);
                    });
        }
        return usersObs;
    }

    private Boolean check(BackendlessUser user, List<Friends> friendsList){
        Boolean state = null;
        for (Friends curentFriends : friendsList){
            state = user.getObjectId().equals(curentFriends.getUser_one().getObjectId()) || user.getObjectId().equals(curentFriends.getUser_two().getObjectId());
            if (state){
                break;
            }
        }
        return state;
    }
}
