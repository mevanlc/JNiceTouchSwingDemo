package io.github.mevanlc.jnicetouch.swingdemo;

import io.github.mevanlc.jnicetouch.EventFlag;
import io.github.mevanlc.jnicetouch.EventPhase;
import io.github.mevanlc.jnicetouch.MacTrackpadEvent;
import java.awt.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import javax.swing.table.AbstractTableModel;

@SuppressWarnings("serial")
final class EventLogModel extends AbstractTableModel {
    private static final long serialVersionUID = 1L;
    private static final int MAX_ROWS = 300;
    private static final String[] COLUMNS = {
        "#", "Kind", "Phase", "Values", "Pinned target", "Sequence", "Flags"
    };

    private final List<EventRow> rows = new ArrayList<>();
    private long nextIndex = 1;

    void add(Component target, MacTrackpadEvent event) {
        rows.add(0, EventRow.from(nextIndex++, target, event));
        fireTableRowsInserted(0, 0);
        if (rows.size() > MAX_ROWS) {
            int removed = rows.size() - 1;
            rows.remove(removed);
            fireTableRowsDeleted(removed, removed);
        }
    }

    void clear() {
        int oldSize = rows.size();
        rows.clear();
        if (oldSize > 0) {
            fireTableRowsDeleted(0, oldSize - 1);
        }
    }

    long totalEvents() {
        return nextIndex - 1;
    }

    @Override
    public int getRowCount() {
        return rows.size();
    }

    @Override
    public int getColumnCount() {
        return COLUMNS.length;
    }

    @Override
    public String getColumnName(int column) {
        return COLUMNS[column];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        EventRow row = rows.get(rowIndex);
        return switch (columnIndex) {
            case 0 -> row.index();
            case 1 -> row.kind();
            case 2 -> row.phase();
            case 3 -> row.values();
            case 4 -> row.target();
            case 5 -> row.sequence();
            case 6 -> row.flags();
            default -> throw new IndexOutOfBoundsException("column " + columnIndex);
        };
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return columnIndex == 0 || columnIndex == 5 ? Long.class : String.class;
    }

    private record EventRow(
            long index,
            String kind,
            String phase,
            String values,
            String target,
            long sequence,
            String flags) {

        static EventRow from(long index, Component target, MacTrackpadEvent event) {
            return new EventRow(
                    index,
                    title(event.kind().name()),
                    formatPhases(event),
                    formatValues(event),
                    targetName(target),
                    event.sequenceId(),
                    formatFlags(event.flags()));
        }

        private static String formatPhases(MacTrackpadEvent event) {
            String physical = join(event.phases());
            String momentum = join(event.momentumPhases());
            if (physical.isEmpty() && momentum.isEmpty()) {
                return "discrete";
            }
            if (momentum.isEmpty()) {
                return physical;
            }
            if (physical.isEmpty()) {
                return "momentum " + momentum;
            }
            return physical + " · momentum " + momentum;
        }

        private static String formatValues(MacTrackpadEvent event) {
            return switch (event.kind()) {
                case SCROLL, SWIPE -> String.format(
                        Locale.ROOT, "Δ %.2f, %.2f", event.deltaX(), event.deltaY());
                case MAGNIFY -> String.format(
                        Locale.ROOT, "%+.4f magnification", event.magnification());
                case ROTATE -> String.format(
                        Locale.ROOT, "%+.2f°", event.rotationDegrees());
                case PRESSURE -> String.format(
                        Locale.ROOT,
                        "%.3f · stage %d · transition %+.3f",
                        event.pressure(), event.pressureStage(), event.stageTransition());
                case SMART_MAGNIFY -> "toggle detail scale";
                case QUICK_LOOK -> "Quick Look";
                case CANCEL -> "sequence state cleared";
            };
        }

        private static String formatFlags(Set<EventFlag> flags) {
            return flags.isEmpty() ? "—" : flags.stream()
                    .sorted()
                    .map(flag -> title(flag.name()))
                    .collect(Collectors.joining(", "));
        }

        private static String join(Set<EventPhase> phases) {
            return phases.stream()
                    .sorted()
                    .map(phase -> phase.name().toLowerCase(Locale.ROOT).replace('_', ' '))
                    .collect(Collectors.joining(" + "));
        }

        private static String targetName(Component target) {
            if (target.getName() != null && !target.getName().isBlank()) {
                return target.getName();
            }
            return target.getClass().getSimpleName();
        }

        private static String title(String value) {
            String lower = value.toLowerCase(Locale.ROOT).replace('_', ' ');
            return Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
        }
    }
}
