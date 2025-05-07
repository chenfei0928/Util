package androidx.recyclerview.widget;

/**
 * Created by MrFeng on 2018/1/8.
 */
public class RecyclerViewCf0928UtilHelper {
    private RecyclerViewCf0928UtilHelper() {
    }

    public static void changeViewType(RecyclerView.ViewHolder viewHolder, int viewType) {
        viewHolder.mItemViewType = viewType;
    }
}
