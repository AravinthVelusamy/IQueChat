package com.iquesoft.andrew.seedprojectchat.view.classes.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.EditText;

import com.backendless.BackendlessUser;
import com.iquesoft.andrew.seedprojectchat.R;
import com.iquesoft.andrew.seedprojectchat.adapters.FindFriendAdapter;
import com.iquesoft.andrew.seedprojectchat.common.BaseFragment;
import com.iquesoft.andrew.seedprojectchat.di.components.IMainActivityComponent;
import com.iquesoft.andrew.seedprojectchat.presenter.classes.fragments.FindFriendFragmentPresenter;
import com.iquesoft.andrew.seedprojectchat.util.ConvertBackendlessUserToChatUser;
import com.iquesoft.andrew.seedprojectchat.view.interfaces.fragments.IFindFriendFragment;

import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import jp.wasabeef.recyclerview.adapters.ScaleInAnimationAdapter;
import jp.wasabeef.recyclerview.animators.ScaleInAnimator;
import rx.Subscription;

/**
 * Created by andrew on 8/31/2016.
 */

public class FindFriendFragment extends BaseFragment implements IFindFriendFragment {

    @Inject
    FindFriendFragmentPresenter presenter;

    @Inject
    ConvertBackendlessUserToChatUser convertBackendlessUserToChatUser;

    @Inject
    FriendsFragment friendsFragment;

    private View rootView;

    @BindView(R.id.edit_text_username)
    EditText username;
    @BindView(R.id.recycler_view_find_user)
    RecyclerView recyclerViewFindUsers;

    private FindFriendAdapter adapter;
    private Subscription getFriendsSubscription;

    @OnClick(R.id.btn_find_users)
    public void findClick(View view) {
        getFriendsSubscription = friendsFragment.getCurentFriendList().subscribe(response ->{
            presenter.getBackendlessUsers(username.getText().toString(), response).subscribe(this::setUserAdapter);
        });
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.fragment_find_friend, container, false);
            ButterKnife.bind(this, rootView);
        }
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        this.getComponent(IMainActivityComponent.class).inject(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        presenter.init(this);
    }

    private void setUserAdapter(List<BackendlessUser> users) {
        adapter = new FindFriendAdapter(users, getActivity());
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        recyclerViewFindUsers.setLayoutManager(linearLayoutManager);
        recyclerViewFindUsers.setItemAnimator(new ScaleInAnimator(new OvershootInterpolator(1f)));
        ScaleInAnimationAdapter scaleInAnimationAdapter = new ScaleInAnimationAdapter(adapter);
        scaleInAnimationAdapter.setFirstOnly(true);
        scaleInAnimationAdapter.setDuration(500);
        recyclerViewFindUsers.setAdapter(scaleInAnimationAdapter);
    }
}
