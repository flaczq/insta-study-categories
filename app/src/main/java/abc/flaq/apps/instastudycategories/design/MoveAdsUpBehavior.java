package abc.flaq.apps.instastudycategories.design;

import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.View;

import com.google.android.gms.ads.AdView;

public class MoveAdsUpBehavior extends CoordinatorLayout.Behavior<AdView> {

    public MoveAdsUpBehavior() {
        super();
    }

    public MoveAdsUpBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, AdView child, View dependency) {
        return (dependency instanceof Snackbar.SnackbarLayout);
    }
    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, AdView child, View dependency) {
        float translationY = Math.min(0, ViewCompat.getTranslationY(dependency) - dependency.getHeight());
        ViewCompat.setTranslationY(child, translationY);
        return true;
    }

}
