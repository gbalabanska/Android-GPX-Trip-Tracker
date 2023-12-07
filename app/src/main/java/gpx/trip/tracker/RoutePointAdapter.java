package gpx.trip.tracker;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

import gpx.trip.tracker.dto.MarkerRoutePoint;
import gpx.trip.tracker.management.DataManager;

public class RoutePointAdapter extends RecyclerView.Adapter<RoutePointAdapter.MyViewHolder> {
    private final Context context;
    private DataManager dataManager;
    ArrayList<MarkerRoutePoint> showMarkerRoutePoints;

    public RoutePointAdapter(Context context) {
        this.context = context;
        dataManager = DataManager.getInstance();
        dataManager.setAdapter(this);
        showMarkerRoutePoints = dataManager.getMarkerRoutePoints();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_route_point, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");

        MarkerRoutePoint markerRoutePoint = showMarkerRoutePoints.get(position);

        String pointName = markerRoutePoint.getRefPoint().getName();
        holder.tvName.setText(pointName);

        SharedPreferences preferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        boolean displayCoordinates = preferences.getBoolean("displayCoordinates", false);
        if (displayCoordinates) {
            double latDouble = markerRoutePoint.getRefPoint().getLat();
            double lonDouble = markerRoutePoint.getRefPoint().getLon();
            String coordinates = context.getString(R.string.coordinates, String.valueOf(latDouble), String.valueOf(lonDouble));
            holder.tvCoordinates.setText(coordinates);
        } else {
            holder.tvCoordinates.setVisibility(View.GONE);
        }
        String rest = context.getString(R.string.rest) + " " + formatSecondsWithoutSign(markerRoutePoint.getRefPoint().getRest());
        holder.tvRest.setText(rest);

        LocalDateTime plannedTime = markerRoutePoint.getPlannedDateTimeOfArrival();
        if (plannedTime != null) {
            String formattedDateTime = plannedTime.format(formatter);
            holder.tvPlannedTimePlaceholder.setText(formattedDateTime);
        }

        if (markerRoutePoint.isReached()) {
            // display actual time
            holder.tvSecondTime.setText(R.string.time_actual);
            holder.tvSecondTime.setTypeface(null, Typeface.BOLD);
            holder.imageView.setImageResource(R.drawable.checked);
            LocalDateTime actualTime = markerRoutePoint.getActualDateTimeOfArrival();
            String formattedDateTime = actualTime.format(formatter);
            holder.tvSecondTimePlaceholder.setText(formattedDateTime);
            holder.tvSecondTimePlaceholder.setTypeface(null, Typeface.BOLD);
            Duration timeDifference = Duration.between(plannedTime, actualTime);
            long secondsDifference = timeDifference.getSeconds();
            if (secondsDifference <= 0) {
                holder.tvDelayHolder.setTextColor(ContextCompat.getColor(context, R.color.checkedGreen));
            } else {
                holder.tvDelayHolder.setTextColor(ContextCompat.getColor(context, R.color.red));
            }
            holder.tvDelayHolder.setText(formatSecondsWithSign(secondsDifference));
        } else {
            // point is not reached yet
            // display predicted time
            holder.imageView.setImageResource(R.drawable.clock);
            LocalDateTime predictedTime = markerRoutePoint.getPredictedDateTimeOfArrival();
            if (predictedTime != null) {
                holder.tvSecondTime.setText(R.string.time_predicted);
                holder.tvSecondTime.setTypeface(null, Typeface.NORMAL);
                String formattedDateTime = predictedTime.format(formatter);
                holder.tvSecondTimePlaceholder.setText(formattedDateTime);
                holder.tvSecondTimePlaceholder.setTypeface(null, Typeface.NORMAL);
                Duration timeDifference = Duration.between(plannedTime, predictedTime);
                long secondsDifference = timeDifference.getSeconds();
                if (secondsDifference <= 0) {
                    holder.tvDelayHolder.setTextColor(ContextCompat.getColor(context, R.color.checkedGreen));
                } else {
                    holder.tvDelayHolder.setTextColor(ContextCompat.getColor(context, R.color.red));
                }
                holder.tvDelayHolder.setText(formatSecondsWithSign(secondsDifference));
            }
        }


    }

    private static String formatSecondsWithSign(long totalSeconds) {
        if (totalSeconds == 0) {
            return "0s";
        }
        char sign = (totalSeconds < 0) ? '-' : '+';
        long absoluteSeconds = Math.abs(totalSeconds);

        long hours = absoluteSeconds / 3600;
        long minutes = (absoluteSeconds % 3600) / 60;
        long seconds = absoluteSeconds % 60;

        if (hours > 0) {
            return String.format("%c%02dh:%02dmin:%02ds", sign, hours, minutes, seconds);
        } else if (minutes > 0) {
            return String.format("%c%02dmin:%02ds", sign, minutes, seconds);
        } else {
            return String.format("%c%02ds", sign, seconds);
        }
    }

    private static String formatSecondsWithoutSign(long totalSeconds) {
        long absoluteSeconds = Math.abs(totalSeconds);

        long hours = absoluteSeconds / 3600;
        long minutes = (absoluteSeconds % 3600) / 60;
        long seconds = absoluteSeconds % 60;

        if (hours > 0) {
            return String.format("%02dh:%02dmin:%02ds", hours, minutes, seconds);
        } else if (minutes > 0) {
            return String.format("%02dmin:%02ds", minutes, seconds);
        } else {
            return String.format("%02ds", seconds);
        }
    }

    @Override
    public int getItemCount() {
        return showMarkerRoutePoints.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvCoordinates, tvRest, tvSecondTime, tvPlannedTimePlaceholder, tvSecondTimePlaceholder, tvDelayHolder;
        ImageView imageView;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvCoordinates = itemView.findViewById(R.id.tvCoordinates);
            tvRest = itemView.findViewById(R.id.tvRest);
            tvSecondTime = itemView.findViewById(R.id.tvSecondTime);
            tvPlannedTimePlaceholder = itemView.findViewById(R.id.tvPlannedTimePlaceholder);
            tvSecondTimePlaceholder = itemView.findViewById(R.id.tvSecondTimePlaceholder);
            tvDelayHolder = itemView.findViewById(R.id.tvDelayHolder);
            imageView = itemView.findViewById(R.id.icon2);

        }
    }
}
