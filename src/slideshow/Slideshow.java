package slideshow;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.Timer;
import net.miginfocom.swing.MigLayout;
import org.jdesktop.animation.timing.Animator;
import org.jdesktop.animation.timing.TimingTarget;
import org.jdesktop.animation.timing.TimingTargetAdapter;

public class Slideshow extends JLayeredPane {

    private final JPanel panel;
    private final Pagination pagination;
    private final Animator animator;
    private final Timer timer;
    private final MigLayout layout;
    private Component componentShow;
    private Component componentOut;
    private int currentIndex;
    private boolean next;

    public Slideshow() {
        setOpaque(true);
        setBackground(new Color(200, 200, 200));
        layout = new MigLayout("inset 0");
        panel = new JPanel();
        pagination = new Pagination();
        pagination.setEventPagination(new EventPagination() {
            @Override
            public void onClick(int pageClick) {
                if (!animator.isRunning()) {
                    if (pageClick != currentIndex) {
                        timer.restart();
                        next = currentIndex < pageClick;
                        if (next) {
                            componentOut = panel.getComponent(checkNext(currentIndex));
                            currentIndex = getNext(pageClick - 1);
                            componentShow = panel.getComponent(currentIndex);
                            animator.start();
                        } else {
                            componentOut = panel.getComponent(checkBack(currentIndex));
                            currentIndex = getBack(pageClick + 1);
                            componentShow = panel.getComponent(currentIndex);
                            animator.start();
                        }
                    }
                }
            }
        });
        TimingTarget target = new TimingTargetAdapter() {
            @Override
            public void begin() {
                componentShow.setVisible(true);
                componentOut.setVisible(true);
                pagination.setIndex(currentIndex);
            }

            @Override
            public void timingEvent(float fraction) {
                double width = panel.getWidth();
                int location = (int) (width * fraction);
                int locationShow = (int) (width * (1f - fraction));
                if (next) {
                    layout.setComponentConstraints(componentShow, "pos " + locationShow + " 0 100% 100%, w 100%!");
                    layout.setComponentConstraints(componentOut, "pos -" + location + " 0 " + (width - location) + " 100%");
                } else {
                    layout.setComponentConstraints(componentShow, "pos -" + locationShow + " 0 " + (width - locationShow) + " 100%");
                    layout.setComponentConstraints(componentOut, "pos " + location + " 0 100% 100%, w 100%!");
                }
                pagination.setAnimation(fraction);
                panel.revalidate();
            }

            @Override
            public void end() {
                componentOut.setVisible(false);
                layout.setComponentConstraints(componentShow, "pos 0 0 100% 100%, width 100%");
            }
        };
        animator = new Animator(1000, target);
        animator.setResolution(0);
        animator.setAcceleration(0.5f);
        animator.setDeceleration(0.5f);
        setLayer(pagination, JLayeredPane.POPUP_LAYER);
        panel.setLayout(layout);
        setLayout(new MigLayout("fill, inset 0", "[fill, center]", "3[fill]3"));
        add(pagination, "pos 0.5al 1al n n");
        add(panel, "w 100%-6!");
        timer = new Timer(3000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                next();
            }
        });
    }

    public void initSlideshow(Component... coms) {
        if (coms.length >= 2) {
            for (Component com : coms) {
                com.setVisible(false);
                panel.add(com, "pos 0 0 0 0");
            }
            if (panel.getComponentCount() > 0) {
                componentShow = panel.getComponent(0);
                componentShow.setVisible(true);
                layout.setComponentConstraints(componentShow, "pos 0 0 100% 100%");
            }
            pagination.setTotalPage(panel.getComponentCount());
            pagination.setCurrentIndex(0);
            timer.start();
        }
    }

    public void next() {
        if (!animator.isRunning()) {
            timer.restart();
            next = true;
            currentIndex = getNext(currentIndex);
            componentShow = panel.getComponent(currentIndex);
            componentOut = panel.getComponent(checkNext(currentIndex - 1));
            animator.start();
        }
    }

    public void back() {
        if (!animator.isRunning()) {
            timer.restart();
            next = false;
            currentIndex = getBack(currentIndex);
            componentShow = panel.getComponent(currentIndex);
            componentOut = panel.getComponent(checkBack(currentIndex + 1));
            animator.start();
        }
    }

    private int getNext(int index) {
        if (index == panel.getComponentCount() - 1) {
            return 0;
        } else {
            return index + 1;
        }
    }

    private int checkNext(int index) {
        if (index == -1) {
            return panel.getComponentCount() - 1;
        } else {
            return index;
        }
    }

    private int getBack(int index) {
        if (index == 0) {
            return panel.getComponentCount() - 1;
        } else {
            return index - 1;
        }
    }

    private int checkBack(int index) {
        if (index == panel.getComponentCount()) {
            return 0;
        } else {
            return index;
        }
    }
}
