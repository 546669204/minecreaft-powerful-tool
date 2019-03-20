package com.github.a546669204.client.config;

import com.github.a546669204.client.KeyLoader;
import com.github.a546669204.client.MobAttack;

import net.minecraft.entity.Entity;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class config {
    private JTextField textField1;
    private JTextField textField2;
    private JList list1;
    private JList list2;
    public JPanel config;
    private JButton saveButton;
    private JButton closeButton;
    private JCheckBox checkBox1;
    private JCheckBox checkBox2;
    private JCheckBox checkBox3;
    private JButton button1;
    private JPopupMenu popupMenu = null;
    private JPopupMenu popupMenu2 = null;

    private String curString = "";

    public config() {

        DefaultListModel listModel1 = new DefaultListModel();

        List<Entity> entities = MobAttack.getPlayerEntity();
        for (int i = 0; i < entities.size(); i++) {
            Entity e = (Entity) entities.get(i);
            listModel1.addElement(e.getName());
        }


        DefaultListModel listModel2 = new DefaultListModel();
        for (String key : ModConfig.autoAttack.target.keySet()) {
            listModel2.addElement(key);
        }
        list1.setModel(listModel1);
        list2.setModel(listModel2);

        //自动防御 选择框
        checkBox1.setSelected(ModConfig.autoAttack.useShiled);
        //自动拾取 不要白色
        checkBox2.setSelected(ModConfig.pickUp.notWhite);
        //自动采集
//        checkBox3.setSelected(ModConfig.pickUp.autoCollection);


        textField1.setText("" + ModConfig.autoAttack.time);
        textField2.setText("" + ModConfig.autoAttack.range);

        popupMenu = new JPopupMenu();
        JMenuItem addItem = new JMenuItem("添加");
        JMenuItem resetItem = new JMenuItem("刷新");
        addItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                listModel2.addElement(curString);
                list2.setModel(listModel2);
            }
        });
        resetItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                list1.setModel(listModel1);
            }
        });
        popupMenu.add(addItem); //添加菜单项Open
        popupMenu.add(resetItem);

        popupMenu2 = new JPopupMenu();
        JMenuItem removeItem = new JMenuItem("删除");
        removeItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                listModel2.removeElement(curString);
                list2.setModel(listModel2);
            }
        });
        popupMenu2.add(removeItem);

        list1.addMouseListener(new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent e) {
                super.mousePressed(e);
                if (e.getButton() == 3) {
                    maybeShowPopup(e);
                }

            }

            //弹出菜单
            private void maybeShowPopup(MouseEvent e) {
                if (list1.getSelectedIndex() != -1) {
                    //获取选择项的值
                    Object selected = list1.getModel().getElementAt(list1.getSelectedIndex());
                    curString = selected.toString();
                    popupMenu.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });

        list2.addMouseListener(new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent e) {
                super.mousePressed(e);
                System.out.println(e.getButton());
                if (e.getButton() == 3) {
                    maybeShowPopup(e);
                }
            }

            //弹出菜单
            private void maybeShowPopup(MouseEvent e) {
                if (list2.getSelectedIndex() != -1) {
                    //获取选择项的值
                    Object selected = list2.getModel().getElementAt(list2.getSelectedIndex());
                    curString = selected.toString();
                    popupMenu2.show(e.getComponent(), e.getX(), e.getY());
                    //123456
                }
            }
        });

        saveButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);

                ModConfig.autoAttack.time = new Double(textField1.getText());
                ModConfig.autoAttack.range = new Double(textField2.getText());

                Object[] arr = listModel2.toArray();
                Map<String, Boolean> map = new HashMap<String, Boolean>();
                for (int i = 0; i < arr.length; i++) {
                    map.put(arr[i].toString(), true);
                }
                ModConfig.autoAttack.target = map;

                ModConfig.autoAttack.useShiled = checkBox1.isSelected();
                ModConfig.pickUp.notWhite = checkBox2.isSelected();
//                ModConfig.pickUp.autoCollection = checkBox3.isSelected();


                ModConfig.store();
                KeyLoader.closeWindow();
            }
        });
        closeButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                KeyLoader.closeWindow();
            }
        });
    }

    private void createUIComponents() {
        // TODO: place custom component creation code here
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        config = new JPanel();
        config.setLayout(new GridBagLayout());
        final JLabel label1 = new JLabel();
        label1.setText("攻击间隔");
        GridBagConstraints gbc;
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        config.add(label1, gbc);
        textField1 = new JTextField();
        textField1.setColumns(10);
        textField1.setText("");
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.gridwidth = 3;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        config.add(textField1, gbc);
        final JLabel label2 = new JLabel();
        label2.setText("攻击范围");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        config.add(label2, gbc);
        closeButton = new JButton();
        closeButton.setText("取消");
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 5;
        gbc.gridwidth = 3;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        config.add(closeButton, gbc);
        final JLabel label3 = new JLabel();
        label3.setText("攻击目标");
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        config.add(label3, gbc);
        final JLabel label4 = new JLabel();
        label4.setText("目标怪物");
        gbc = new GridBagConstraints();
        gbc.gridx = 4;
        gbc.gridy = 3;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        config.add(label4, gbc);
        final JScrollPane scrollPane1 = new JScrollPane();
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        config.add(scrollPane1, gbc);
        list1 = new JList();
        list1.setEnabled(true);
        final DefaultListModel defaultListModel1 = new DefaultListModel();
        list1.setModel(defaultListModel1);
        list1.setSelectionMode(0);
        scrollPane1.setViewportView(list1);
        final JScrollPane scrollPane2 = new JScrollPane();
        gbc = new GridBagConstraints();
        gbc.gridx = 4;
        gbc.gridy = 4;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        config.add(scrollPane2, gbc);
        list2 = new JList();
        final DefaultListModel defaultListModel2 = new DefaultListModel();
        list2.setModel(defaultListModel2);
        list2.setSelectionMode(0);
        list2.putClientProperty("List.isFileList", Boolean.FALSE);
        scrollPane2.setViewportView(list2);
        textField2 = new JTextField();
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 1;
        gbc.gridwidth = 3;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        config.add(textField2, gbc);
        final JLabel label5 = new JLabel();
        label5.setHorizontalTextPosition(0);
        label5.setText("世界中已有怪物");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        config.add(label5, gbc);
        saveButton = new JButton();
        saveButton.setText("保存");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        config.add(saveButton, gbc);
        checkBox1 = new JCheckBox();
        checkBox1.setText("自动防御");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        config.add(checkBox1, gbc);
        final JLabel label6 = new JLabel();
        label6.setText("拾取控制");
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 2;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        config.add(label6, gbc);
        checkBox2 = new JCheckBox();
        checkBox2.setText("不捡白装 ");
        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 2;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        config.add(checkBox2, gbc);
        checkBox3 = new JCheckBox();
        checkBox3.setText("自动采集");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.WEST;
        config.add(checkBox3, gbc);
        label1.setLabelFor(textField1);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return config;
    }

}
