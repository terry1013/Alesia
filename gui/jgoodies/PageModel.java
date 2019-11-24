package gui.jgoodies;

import com.jgoodies.common.base.Preconditions;
import com.jgoodies.navigation.views.AbstractViewModel;
import com.jgoodies.navigation.views.View;
import java.util.Objects;
import java.util.Stack;

public final class PageModel extends AbstractViewModel<Page> {
	public static final String PROPERTY_CURRENT_PAGE = "currentPage";

	public static final String PROPERTY_CAN_GO_BACK = "canGoBack";

	private final Stack<PageEntry> backStack = new Stack();

	private PageEntry currentPageEntry;

	public Page getCurrentPage() {
		return (this.currentPageEntry == null) ? null : this.currentPageEntry.getSourcePage();
	}

	public Page getPreviousPage() {
		PageEntry entry = (PageEntry) this.backStack.peek();
		return entry.getSourcePage();
	}

	public void goBack() {
		boolean oldCanGoBack = canGoBack();
		Preconditions.checkState(oldCanGoBack, "Back is not possible");
		PageEntry mostRecentEntry = (PageEntry) this.backStack.peek();
		navigate0(mostRecentEntry.getSourcePage(), mostRecentEntry.getParameter(), NavigationMode.BACK);
	}

	public boolean canGoBack() {
		return !this.backStack.isEmpty();
	}

	public void clearBackStack() {
		this.backStack.clear();
		firePropertyChange("canGoBack", null, Boolean.valueOf(canGoBack()));
	}

	public boolean navigate(Page page) {
		return navigate(page, null);
	}

	public boolean navigate(Page page, Object parameter) {
		Preconditions.checkNotNull(page, "The %s must not be null.", new Object[]{"page"});
		NavigationMode mode = (page.getPageModel() == null) ? NavigationMode.NEW : NavigationMode.FORWARD;
		return navigate0(page, parameter, mode);
	}

	private boolean navigate0(Page page, Object parameter, NavigationMode mode) {
		PageEntry newPageEntry = new PageEntry(page, parameter);
		PageEntry oldPageEntry = this.currentPageEntry;
		Page oldPage = (oldPageEntry == null) ? null : oldPageEntry.getSourcePage();
		if (Objects.equals(oldPage, page)) {
			page.onNavigatedTo(new NavigationEventArgs(page, parameter, NavigationMode.REFRESH));
			this.currentPageEntry = newPageEntry;
			return true;
		}
		NavigationEventArgs args = new NavigationEventArgs(page, parameter, mode);
		boolean oldCanGoBack = canGoBack();
		if (oldPage != null) {
			if (!oldPage.onNavigatingFrom(args))
				return false;
			if (mode == NavigationMode.BACK && !this.backStack.isEmpty())
				this.backStack.pop();
			oldPage.onNavigatedFrom(args);
		}
		if (this.currentPageEntry != null && mode != NavigationMode.BACK)
			this.backStack.push(this.currentPageEntry);
		this.currentPageEntry = newPageEntry;
		page.setPageModel(this);
		page.onNavigatedTo(args);
		firePropertyChange("canGoBack", oldCanGoBack, canGoBack());
		firePropertyChange("currentPage", oldPage, page);
		firePropertyChange("selectedView", oldPage, page);
		return true;
	}

	protected Page getSelectedView() {
		return getCurrentPage();
	}

	static final class PageEntry {
		private final Page sourcePage;

		private final Object parameter;

		PageEntry(Page sourcePage, Object parameter) {
			this.sourcePage = sourcePage;
			this.parameter = parameter;
		}

		public Page getSourcePage() {
			return this.sourcePage;
		}

		public Object getParameter() {
			return this.parameter;
		}
	}
}
