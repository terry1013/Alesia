package gui.tree;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.tree.*;

public class JTreeExample
{
    public static void main( final String[] args ) throws Exception
    {
        UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );

        // The only correct way to create a SWING Frame...
        EventQueue.invokeAndWait( new Runnable()
            {
                @Override
                public void run()
                {
                    swingMain();
                }
            } );
    }

    protected static void swingMain()
    {
        final JFrame f = new JFrame( "JTree Test" );
        f.setLocationByPlatform( true );
        f.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );

        final int items = 5;

        final DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode( "JTree", true );
        final DefaultTreeModel myModel = new DefaultTreeModel( rootNode );

        final Box buttonBox = new Box( BoxLayout.X_AXIS );

        for( int i = 0; i < items; i++ )
        {
            final String name = "Node " + i;
            final DefaultMutableTreeNode newChild = new DefaultMutableTreeNode( name );
            rootNode.add( newChild );

            final JButton b = new JButton( "Show/Hide " + i );
            buttonBox.add( b );
            b.addActionListener( new ActionListener() {
				
                    @Override
                    public void actionPerformed( final ActionEvent e )
                    {
                        // If the node has a Text, set it to null, otherwise reset it
                        newChild.setUserObject( newChild.getUserObject() == null ? name : null );
                        myModel.nodeStructureChanged( newChild.getParent() );
                    }
                } );
        }

        final JTree tree = new JTree( myModel );
        tree.setRowHeight( 0 );
        tree.setCellRenderer( new JTreeExample.TreeRenderer() );

        f.add( tree, BorderLayout.CENTER );
        f.add( buttonBox, BorderLayout.SOUTH );

        f.setSize( 600, 500 );
        f.setVisible( true );
    }

    public static class TreeRenderer extends DefaultTreeCellRenderer
    {
        @Override
        public Component getTreeCellRendererComponent( final JTree tree, final Object value, final boolean selected,
                                                        final boolean expanded, final boolean leaf, final int row, final boolean hasFocus )
        {
            // Invoke default Implementation, setting all values of this
            super.getTreeCellRendererComponent( tree, value, selected, expanded, leaf, row, hasFocus );

            if( !isNodeVisible( (DefaultMutableTreeNode)value ) )
            {
                setPreferredSize( new Dimension( 0, 0 ) );
            }
            else
            {
                setPreferredSize( new Dimension( 200, 15 ) );
            }

            return this;
        }
    }

    public static boolean isNodeVisible( final DefaultMutableTreeNode value )
    {
        // In this example all Nodes without a UserObject are invisible
        return value.getUserObject() != null;
    }
}