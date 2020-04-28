/* Copyright (C) 2001, 2009 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.applications.gos.awt;

import gov.nasa.worldwind.*;
import gov.nasa.worldwind.applications.gos.*;
import gov.nasa.worldwind.applications.gos.html.HTMLFormatter;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.examples.util.SectorSelector;
import gov.nasa.worldwind.util.*;
import org.w3c.dom.Element;

import javax.swing.*;
import javax.xml.xpath.XPath;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

/**
 * @author dcollins
 * @version $Id: SearchOptionsPanel.java 13555 2010-07-15 16:49:17Z dcollins $
 */
public class SearchOptionsPanel extends JPanel
{
    protected WorldWindow wwd;
    protected JRadioButton anyRegionChoice;
    protected JRadioButton selectedRegionChoice;
    protected JButton regionSelectButton;
    protected SectorSelector sectorSelector;
    protected JComboBox sortOrderBox;
    protected final Set<String> contentTypeSet = new HashSet<String>();
    protected final Set<String> dataCategorySet = new HashSet<String>();
    protected JPanel contentPanel;
    protected JScrollPane scrollPane;

    public SearchOptionsPanel()
    {
        this.contentPanel = new JPanel();
        this.contentPanel.setLayout(new BoxLayout(this.contentPanel, BoxLayout.Y_AXIS));
        this.contentPanel.setBackground(Color.WHITE);
        this.scrollPane = new JScrollPane(this.contentPanel,
            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        this.scrollPane.setBorder(BorderFactory.createEmptyBorder());

        this.setLayout(new BorderLayout(0, 0)); // hgap, vgap
        this.contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // top, left, bottom, right);
        this.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(201, 215, 241)), // top, left, bottom, right
            BorderFactory.createEmptyBorder(0, 0, 0, 10))); // top, left, bottom, right
        this.setBackground(Color.WHITE);
        this.layoutComponents();
    }

    public void setEnabled(boolean enabled)
    {
        super.setEnabled(enabled);
        AWTUtil.setTreeEnabled(this, enabled);
        this.updateRegionChoice();
    }

    public WorldWindow getWorldWindow()
    {
        return this.wwd;
    }

    public void setWorldWindow(WorldWindow wwd)
    {
        if (this.sectorSelector != null)
        {
            this.sectorSelector.disable();
            this.sectorSelector = null;
        }

        this.wwd = wwd;

        this.anyRegionChoice.setSelected(true);
        this.updateRegionChoice();
    }

    public void getParams(AVList outParams)
    {
        if (this.selectedRegionChoice.isSelected() && this.sectorSelector != null &&
            this.sectorSelector.getSector() != null)
        {
            outParams.setValue(GeodataKey.BBOX, this.sectorSelector.getSector());
        }

        if (this.contentTypeSet != null && this.contentTypeSet.size() > 0)
        {
            outParams.setValue(GeodataKey.CONTENT_TYPE_LIST, Collections.unmodifiableSet(this.contentTypeSet));
        }

        if (this.dataCategorySet != null && this.dataCategorySet.size() > 0)
        {
            outParams.setValue(GeodataKey.DATA_CATEGORY_LIST, Collections.unmodifiableSet(this.dataCategorySet));
        }

        if (this.sortOrderBox != null && this.sortOrderBox.getSelectedItem() != null &&
            this.sortOrderBox.getSelectedItem() instanceof Category)
        {
            outParams.setValue(GeodataKey.SORT_ORDER, ((Category) this.sortOrderBox.getSelectedItem()).getKey());
        }
    }

    protected void layoutComponents()
    {
        this.contentPanel.add(new JLabel("Region"));
        this.addRegionComponents(this.contentPanel);

        Element el = Configuration.getElement("//ContentTypeList");
        if (el != null)
        {
            this.contentPanel.add(Box.createVerticalStrut(20));
            this.contentPanel.add(new JLabel("Content Type"));
            this.addCategoryCheckBoxes(el, this.contentTypeSet, this.contentPanel);
        }

        el = Configuration.getElement("//DataCategoryList");
        if (el != null)
        {
            this.contentPanel.add(Box.createVerticalStrut(20));
            this.contentPanel.add(new JLabel("Data Category"));
            this.addCategoryCheckBoxes(el, this.dataCategorySet, this.contentPanel);
        }

        el = Configuration.getElement("//SortOrderList");
        if (el != null)
        {
            this.contentPanel.add(Box.createVerticalStrut(20));
            this.contentPanel.add(new JLabel("Order By"));
            this.sortOrderBox = this.createCategoryChoiceBox(el);
            this.contentPanel.add(this.sortOrderBox);
        }

        this.add(this.scrollPane, BorderLayout.CENTER);
        this.validate();
    }

    //**************************************************************//
    //********************  Region Components  *********************//
    //**************************************************************//

    protected void addRegionComponents(Container container)
    {
        ActionListener regionChoiceListener = new ActionListener()
        {
            public void actionPerformed(ActionEvent event)
            {
                updateRegionChoice();
            }
        };

        this.anyRegionChoice = new JRadioButton("Any Region");
        this.selectedRegionChoice = new JRadioButton("Selected Region:");
        this.regionSelectButton = new JButton(new EnableSelectorAction());

        this.anyRegionChoice.setOpaque(false);
        this.selectedRegionChoice.setOpaque(false);

        this.anyRegionChoice.setAlignmentX(Component.LEFT_ALIGNMENT);
        this.selectedRegionChoice.setAlignmentX(Component.LEFT_ALIGNMENT);
        this.regionSelectButton.setAlignmentX(Component.LEFT_ALIGNMENT);

        this.anyRegionChoice.addActionListener(regionChoiceListener);
        this.selectedRegionChoice.addActionListener(regionChoiceListener);

        ButtonGroup group = new ButtonGroup();
        group.add(this.anyRegionChoice);
        group.add(this.selectedRegionChoice);

        Box box = Box.createHorizontalBox();
        box.setAlignmentX(Component.LEFT_ALIGNMENT);
        box.add(this.selectedRegionChoice);
        box.add(this.regionSelectButton);

        container.add(this.anyRegionChoice);
        container.add(box);
    }

    protected void updateRegionChoice()
    {
        this.anyRegionChoice.setSelected(this.wwd == null);
        this.anyRegionChoice.setEnabled(this.isEnabled());
        this.selectedRegionChoice.setEnabled(this.isEnabled() && this.wwd != null);

        if (this.anyRegionChoice.isSelected())
        {
            this.disableSectorSelector();
            this.regionSelectButton.setAction(new EnableSelectorAction());
        }

        this.regionSelectButton.setEnabled(this.isEnabled() && this.wwd != null
            && this.selectedRegionChoice.isSelected());
    }

    protected void enableSectorSelector()
    {
        if (this.wwd == null)
            return;

        if (this.sectorSelector != null)
            return;

        this.sectorSelector = new SectorSelector(this.wwd);
        this.sectorSelector.setInteriorColor(new Color(1f, 1f, 1f, 0.1f));
        this.sectorSelector.setBorderColor(new Color(1f, 0f, 0f, 0.5f));
        this.sectorSelector.setBorderWidth(3);
        this.sectorSelector.enable();
    }

    protected void disableSectorSelector()
    {
        if (this.sectorSelector == null)
            return;

        this.sectorSelector.disable();
        this.sectorSelector = null;
    }

    protected class EnableSelectorAction extends AbstractAction
    {
        public EnableSelectorAction()
        {
            super("Select");
        }

        public void actionPerformed(ActionEvent event)
        {
            enableSectorSelector();
            ((AbstractButton) event.getSource()).setAction(new DisableSelectorAction());
        }
    }

    protected class DisableSelectorAction extends AbstractAction
    {
        public DisableSelectorAction()
        {
            super("Clear");
        }

        public void actionPerformed(ActionEvent event)
        {
            disableSectorSelector();
            ((AbstractButton) event.getSource()).setAction(new EnableSelectorAction());
        }
    }

    //**************************************************************//
    //********************  Category Components  *******************//
    //**************************************************************//

    protected static class Category
    {
        protected final String key;
        protected final String displayName;

        public Category(String key, String displayName)
        {
            this.key = key;
            this.displayName = displayName;
        }

        public static Category fromDocument(Element domElement, XPath xpath)
        {
            String key = WWXML.getText(domElement, "@key", xpath);
            String displayName = WWXML.getText(domElement, "@displayName", xpath);
            if (WWUtil.isEmpty(key) || WWUtil.isEmpty(displayName))
                return null;

            return new Category(key, displayName);
        }

        public String getKey()
        {
            return this.key;
        }

        public String getDisplayName()
        {
            return this.displayName;
        }

        public String toString()
        {
            return this.getDisplayName();
        }
    }

    protected static class CategorySetAction extends AbstractAction
    {
        protected final Category category;
        protected final Set<String> keySet;

        public CategorySetAction(String text, Category category, Set<String> keySet)
        {
            super((text != null) ? text : category.toString());
            this.category = category;
            this.keySet = keySet;
        }

        public void actionPerformed(ActionEvent e)
        {
            if (((AbstractButton) e.getSource()).isSelected())
            {
                this.keySet.add(this.category.getKey());
            }
            else
            {
                this.keySet.remove(this.category.getKey());
            }
        }
    }

    protected JComboBox createCategoryChoiceBox(Element context)
    {
        XPath xpath = WWXML.makeXPath();
        Element[] els = WWXML.getElements(context, "./Category", xpath);
        if (els == null || els.length == 0)
            return null;

        ArrayList<Category> list = new ArrayList<Category>();

        for (Element el : els)
        {
            if (el == null)
                continue;

            Category category = Category.fromDocument(el, xpath);
            if (category == null)
                continue;

            list.add(category);
        }

        Category[] array = list.toArray(new Category[list.size()]);
        JComboBox comboBox = new JComboBox(array);
        comboBox.setEditable(false);
        comboBox.setAlignmentX(Component.LEFT_ALIGNMENT);

        Dimension d = comboBox.getPreferredSize();
        comboBox.setMaximumSize(new Dimension(d));

        return comboBox;
    }

    protected void addCategoryCheckBoxes(Element context, Set<String> keySet, Container container)
    {
        XPath xpath = WWXML.makeXPath();
        Element[] els = WWXML.getElements(context, "./Category", xpath);
        if (els == null || els.length == 0)
            return;

        Element resourceList = Configuration.getElement("//ResourceList");

        for (Element el : els)
        {
            if (el == null)
                continue;

            Category category = Category.fromDocument(el, xpath);
            if (category == null)
                continue;

            Object imageSource = null;
            if (resourceList != null)
            {
                String s = WWXML.getText(el, "@smallImageResource");
                if (!WWUtil.isEmpty(s))
                    imageSource = ResourceUtil.getResourceReference(s);
            }

            String text = category.getDisplayName();
            if (imageSource != null)
            {
                StringBuilder sb = new StringBuilder();
                HTMLFormatter formatter = new HTMLFormatter();
                formatter.beginHTMLBody(sb);
                formatter.addImage(sb, imageSource, category.getDisplayName());
                formatter.addText(sb, category.getDisplayName(), -1);
                formatter.endHTMLBody(sb);
                text = sb.toString();
            }

            JCheckBox checkBox = new JCheckBox(new CategorySetAction(text, category, keySet));
            checkBox.setOpaque(false);
            checkBox.setAlignmentX(Component.LEFT_ALIGNMENT);
            container.add(checkBox);
        }
    }
}
