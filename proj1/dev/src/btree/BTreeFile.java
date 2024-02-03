/*
 * @(#) bt.java   98/03/24
 * Copyright (c) 1998 UW.  All Rights Reserved.
 *         Author: Xiaohu Li (xioahu@cs.wisc.edu).
 *
 */

/*
 *         CSE 4331/5331 B+ Tree Project (Spring 2024)
 *         Instructor: Abhishek Santra
 *
 */

package btree;

import java.io.*;

import diskmgr.*;
import bufmgr.*;
import global.*;
import heap.*;
import btree.*;

/**
 * btfile.java This is the main definition of class BTreeFile, which derives
 * from abstract base class IndexFile. It provides an insert/delete interface.
 */
public class BTreeFile extends IndexFile implements GlobalConst {

    private final static int MAGIC0 = 1989;

    private final static String lineSep = System.getProperty("line.separator");

    private static FileOutputStream fos;
    private static DataOutputStream trace;

    /**
     * It causes a structured trace to be written to a file. This output is used
     * to drive a visualization tool that shows the inner workings of the b-tree
     * during its operations.
     *
     * @param filename
     *                 input parameter. The trace file name
     * @exception IOException
     *                        error from the lower layer
     */
    public static void traceFilename(String filename) throws IOException {

        fos = new FileOutputStream(filename);
        trace = new DataOutputStream(fos);
    }

    /**
     * Stop tracing. And close trace file.
     *
     * @exception IOException
     *                        error from the lower layer
     */
    public static void destroyTrace() throws IOException {
        if (trace != null)
            trace.close();
        if (fos != null)
            fos.close();
        fos = null;
        trace = null;
    }

    private BTreeHeaderPage headerPage;
    private PageId headerPageId;
    private String dbname;

    /**
     * Access method to data member.
     * 
     * @return Return a BTreeHeaderPage object that is the header page of this
     *         btree file.
     */
    public BTreeHeaderPage getHeaderPage() {
        return headerPage;
    }

    private PageId get_file_entry(String filename) throws GetFileEntryException {
        try {
            return SystemDefs.JavabaseDB.get_file_entry(filename);
        } catch (Exception e) {
            e.printStackTrace();
            throw new GetFileEntryException(e, "");
        }
    }

    private Page pinPage(PageId pageno) throws PinPageException {
        try {
            Page page = new Page();
            SystemDefs.JavabaseBM.pinPage(pageno, page, false/* Rdisk */);
            return page;
        } catch (Exception e) {
            e.printStackTrace();
            throw new PinPageException(e, "");
        }
    }

    private void add_file_entry(String fileName, PageId pageno)
            throws AddFileEntryException {
        try {
            SystemDefs.JavabaseDB.add_file_entry(fileName, pageno);
        } catch (Exception e) {
            e.printStackTrace();
            throw new AddFileEntryException(e, "");
        }
    }

    private void unpinPage(PageId pageno) throws UnpinPageException {
        try {
            SystemDefs.JavabaseBM.unpinPage(pageno, false /* = not DIRTY */);
        } catch (Exception e) {
            e.printStackTrace();
            throw new UnpinPageException(e, "");
        }
    }

    private void freePage(PageId pageno) throws FreePageException {
        try {
            SystemDefs.JavabaseBM.freePage(pageno);
        } catch (Exception e) {
            e.printStackTrace();
            throw new FreePageException(e, "");
        }

    }

    private void delete_file_entry(String filename)
            throws DeleteFileEntryException {
        try {
            SystemDefs.JavabaseDB.delete_file_entry(filename);
        } catch (Exception e) {
            e.printStackTrace();
            throw new DeleteFileEntryException(e, "");
        }
    }

    private void unpinPage(PageId pageno, boolean dirty)
            throws UnpinPageException {
        try {
            SystemDefs.JavabaseBM.unpinPage(pageno, dirty);
        } catch (Exception e) {
            e.printStackTrace();
            throw new UnpinPageException(e, "");
        }
    }

    /**
     * BTreeFile class an index file with given filename should already exist;
     * this opens it.
     *
     * @param filename
     *                 the B+ tree file name. Input parameter.
     * @exception GetFileEntryException
     *                                   can not ger the file from DB
     * @exception PinPageException
     *                                   failed when pin a page
     * @exception ConstructPageException
     *                                   BT page constructor failed
     */
    public BTreeFile(String filename) throws GetFileEntryException,
            PinPageException, ConstructPageException {

        headerPageId = get_file_entry(filename);

        headerPage = new BTreeHeaderPage(headerPageId);
        dbname = new String(filename);
        /*
         * 
         * - headerPageId is the PageId of this BTreeFile's header page; -
         * headerPage, headerPageId valid and pinned - dbname contains a copy of
         * the name of the database
         */
    }

    /**
     * if index file exists, open it; else create it.
     *
     * @param filename
     *                       file name. Input parameter.
     * @param keytype
     *                       the type of key. Input parameter.
     * @param keysize
     *                       the maximum size of a key. Input parameter.
     * @param delete_fashion
     *                       full delete or naive delete. Input parameter. It is
     *                       either
     *                       DeleteFashion.NAIVE_DELETE or
     *                       DeleteFashion.FULL_DELETE.
     * @exception GetFileEntryException
     *                                   can not get file
     * @exception ConstructPageException
     *                                   page constructor failed
     * @exception IOException
     *                                   error from lower layer
     * @exception AddFileEntryException
     *                                   can not add file into DB
     */
    public BTreeFile(String filename, int keytype, int keysize,
            int delete_fashion) throws GetFileEntryException,
            ConstructPageException, IOException, AddFileEntryException {

        headerPageId = get_file_entry(filename);
        if (headerPageId == null) // file not exist
        {
            headerPage = new BTreeHeaderPage();
            headerPageId = headerPage.getPageId();
            add_file_entry(filename, headerPageId);
            headerPage.set_magic0(MAGIC0);
            headerPage.set_rootId(new PageId(INVALID_PAGE));
            headerPage.set_keyType((short) keytype);
            headerPage.set_maxKeySize(keysize);
            headerPage.set_deleteFashion(delete_fashion);
            headerPage.setType(NodeType.BTHEAD);
        } else {
            headerPage = new BTreeHeaderPage(headerPageId);
        }

        dbname = new String(filename);

    }

    /**
     * Close the B+ tree file. Unpin header page.
     *
     * @exception PageUnpinnedException
     *                                        error from the lower layer
     * @exception InvalidFrameNumberException
     *                                        error from the lower layer
     * @exception HashEntryNotFoundException
     *                                        error from the lower layer
     * @exception ReplacerException
     *                                        error from the lower layer
     */
    public void close() throws PageUnpinnedException,
            InvalidFrameNumberException, HashEntryNotFoundException,
            ReplacerException {
        if (headerPage != null) {
            SystemDefs.JavabaseBM.unpinPage(headerPageId, true);
            headerPage = null;
        }
    }

    /**
     * Destroy entire B+ tree file.
     *
     * @exception IOException
     *                                     error from the lower layer
     * @exception IteratorException
     *                                     iterator error
     * @exception UnpinPageException
     *                                     error when unpin a page
     * @exception FreePageException
     *                                     error when free a page
     * @exception DeleteFileEntryException
     *                                     failed when delete a file from DM
     * @exception ConstructPageException
     *                                     error in BT page constructor
     * @exception PinPageException
     *                                     failed when pin a page
     */
    public void destroyFile() throws IOException, IteratorException,
            UnpinPageException, FreePageException, DeleteFileEntryException,
            ConstructPageException, PinPageException {
        if (headerPage != null) {
            PageId pgId = headerPage.get_rootId();
            if (pgId.pid != INVALID_PAGE)
                _destroyFile(pgId);
            unpinPage(headerPageId);
            freePage(headerPageId);
            delete_file_entry(dbname);
            headerPage = null;
        }
    }

    private void _destroyFile(PageId pageno) throws IOException,
            IteratorException, PinPageException, ConstructPageException,
            UnpinPageException, FreePageException {

        BTSortedPage sortedPage;
        Page page = pinPage(pageno);
        sortedPage = new BTSortedPage(page, headerPage.get_keyType());

        if (sortedPage.getType() == NodeType.INDEX) {
            BTIndexPage indexPage = new BTIndexPage(page,
                    headerPage.get_keyType());
            RID rid = new RID();
            PageId childId;
            KeyDataEntry entry;
            for (entry = indexPage.getFirst(rid); entry != null; entry = indexPage
                    .getNext(rid)) {
                childId = ((IndexData) (entry.data)).getData();
                _destroyFile(childId);
            }
        } else { // BTLeafPage

            unpinPage(pageno);
            freePage(pageno);
        }

    }

    private void updateHeader(PageId newRoot) throws IOException,
            PinPageException, UnpinPageException {

        BTreeHeaderPage header;
        PageId old_data;

        header = new BTreeHeaderPage(pinPage(headerPageId));

        old_data = headerPage.get_rootId();
        header.set_rootId(newRoot);

        // clock in dirty bit to bm so our dtor needn't have to worry about it
        unpinPage(headerPageId, true /* = DIRTY */);

        // ASSERTIONS:
        // - headerPage, headerPageId valid, pinned and marked as dirty

    }

    /**
     * insert record with the given key and rid
     *
     * @param key
     *            the key of the record. Input parameter.
     * @param rid
     *            the rid of the record. Input parameter.
     * @exception KeyTooLongException
     *                                    key size exceeds the max keysize.
     * @exception KeyNotMatchException
     *                                    key is not integer key nor string key
     * @exception IOException
     *                                    error from the lower layer
     * @exception LeafInsertRecException
     *                                    insert error in leaf page
     * @exception IndexInsertRecException
     *                                    insert error in index page
     * @exception ConstructPageException
     *                                    error in BT page constructor
     * @exception UnpinPageException
     *                                    error when unpin a page
     * @exception PinPageException
     *                                    error when pin a page
     * @exception NodeNotMatchException
     *                                    node not match index page nor leaf page
     * @exception ConvertException
     *                                    error when convert between revord and byte
     *                                    array
     * @exception DeleteRecException
     *                                    error when delete in index page
     * @exception IndexSearchException
     *                                    error when search
     * @exception IteratorException
     *                                    iterator error
     * @exception LeafDeleteException
     *                                    error when delete in leaf page
     * @exception InsertException
     *                                    error when insert in index page
     */
    public void insert(KeyClass key, RID rid) throws KeyTooLongException,
            KeyNotMatchException, LeafInsertRecException,
            IndexInsertRecException, ConstructPageException,
            UnpinPageException, PinPageException, NodeNotMatchException,
            ConvertException, DeleteRecException, IndexSearchException,
            IteratorException, LeafDeleteException, InsertException,
            IOException

    {
        /*
         * Start with validating the key [in our case it is integer
         * (attrInteger)]. If the tree is empty, create first page as newrootPage of
         * type which will be a leaf page and set its page id to the headerpageId (already
         * initialized in BTreeFile() constructor) and set its next page and previous page pointer
         * to INVALID_PAGE; insert the <key,rid> using the insertRecord() method then
         * unpin the page as it is dirty (it is already pinned when you get from the
         * buffer) and update the header page using updateHeader() else make a call to
         * _insert()[newRootEntry=_insert(key,rid,headerPage.get_rootId());]
         * method to insert the record <key, data> and set the pointers. If a page
         * overflows (i.e., no space for the new entry), you should split the page. You may have
         * to insert additional entries of the form <key, id of child page> into the higher-level
         * index pages as part of a split. Note that this could recursively go all the way up
         * to the root, possibly resulting in a split of the root node of the B+ tree.
         */

        // keyType check
        short keyType = headerPage.get_keyType();
        if (keyType != AttrType.attrInteger && keyType != AttrType.attrString) {
            throw new Error("insert doesn't support the given AttrType");
        }

        // null root check
        if (headerPage.get_rootId().pid == INVALID_PAGE) {
            BTLeafPage newRootPage = new BTLeafPage(keyType);
            PageId newRoot = newRootPage.getCurPage();

            newRootPage.setPrevPage(new PageId(INVALID_PAGE));
            newRootPage.setNextPage(new PageId(INVALID_PAGE));

            newRootPage.insertRecord(key, rid);

            unpinPage(newRoot, true);
            updateHeader(newRoot);
        } else {
            // _insert(key, rid, headerPage.get_rootId());
        }

        // TODO Insert() method - https://github.com/lankm/uta-dbms/issues/4
        // [ASantra: 1/14/2024] Write your code here
    }

    private KeyDataEntry _insert(KeyClass key, RID rid, PageId currentPageId)
            throws PinPageException, IOException, ConstructPageException,
            LeafDeleteException, ConstructPageException, DeleteRecException,
            IndexSearchException, UnpinPageException, LeafInsertRecException,
            ConvertException, IteratorException, IndexInsertRecException,
            KeyNotMatchException, NodeNotMatchException, InsertException

    {
        // BTSortedPage page = new BTSortedPage(currentPageId,
        // headerPage.get_keyType());
        // if (page.getType() == NodeType.INDEX) {
        // // Page is an index node
        // page = new BTIndexPage(page, headerPage.get_keyType());
        // RID nextRID = page.firstRecord();
        // while (nextRID != /*INVALID_PAGE*/) {

        // nextRID = page.nextRecord(nextRID);
        // }

        // } else if (page.getType() == NodeType.LEAF){
        // // Page is a leaf node
        // page = new BTLeafPage(page, headerPage.get_keyType());

        // } else {
        // throw new IllegalArgumentException("Invalid page type: " + page.getType() + "
        // is not an index or leaf page")
        // }
        /*
         * Check the pageType. If it is an INDEX page call _insert() recursively to
         * insert and split if necessary. If it a LEAF page again call _insert()
         * recursively to insert and handle split.
         * 
         * The keyCompare() method is used by _insert()to compare the key.
         * 
         * int keyCompare(KeyClass key1, KeyClass key2)
         * 
         * This method returns an integer. key1>key2 returns a positive value, key1<key2
         * will return a negative value and 0 if equal (key1 == key2).
         * 
         * NOTE: As discussed in the class, condition for traversal can be chosen, but
         * needs to be consistent for an implementation. Clearly state in the report, which
         * traversal choice you chose for your implementation, Choice1) the right pointer needs to
         * be traversed if key1 value is greater than or equal to key2; else traverse the
         * left pointer.
         * OR Choice2) the right pointer needs to be traversed if key1 value is greater
         * than key2; else traverse the left pointer
         */

        // TODO _insert() method - https://github.com/lankm/uta-dbms/issues/4
        // [ASantra: 1/14/2024] Remove the return statement and start your code.
        return null;
    }

    /**
     * delete leaf entry given its <key, rid> pair. `rid' is IN the data entry;
     * it is not the id of the data entry)
     *
     * @param key
     *            the key in pair <key, rid>. Input Parameter.
     * @param rid
     *            the rid in pair <key, rid>. Input Parameter.
     * @return true if deleted. false if no such record.
     * @exception DeleteFashionException
     *                                      neither full delete nor naive delete
     * @exception LeafRedistributeException
     *                                      redistribution error in leaf pages
     * @exception RedistributeException
     *                                      redistribution error in index pages
     * @exception InsertRecException
     *                                      error when insert in index page
     * @exception KeyNotMatchException
     *                                      key is neither integer key nor string
     *                                      key
     * @exception UnpinPageException
     *                                      error when unpin a page
     * @exception IndexInsertRecException
     *                                      error when insert in index page
     * @exception FreePageException
     *                                      error in BT page constructor
     * @exception RecordNotFoundException
     *                                      error delete a record in a BT page
     * @exception PinPageException
     *                                      error when pin a page
     * @exception IndexFullDeleteException
     *                                      fill delete error
     * @exception LeafDeleteException
     *                                      delete error in leaf page
     * @exception IteratorException
     *                                      iterator error
     * @exception ConstructPageException
     *                                      error in BT page constructor
     * @exception DeleteRecException
     *                                      error when delete in index page
     * @exception IndexSearchException
     *                                      error in search in index pages
     * @exception IOException
     *                                      error from the lower layer
     *
     */
    public boolean Delete(KeyClass key, RID rid) throws DeleteFashionException,
            LeafRedistributeException, RedistributeException,
            InsertRecException, KeyNotMatchException, UnpinPageException,
            IndexInsertRecException, FreePageException,
            RecordNotFoundException, PinPageException,
            IndexFullDeleteException, LeafDeleteException, IteratorException,
            ConstructPageException, DeleteRecException, IndexSearchException,
            IOException {
        if (headerPage.get_deleteFashion() == DeleteFashion.NAIVE_DELETE)
            return NaiveDelete(key, rid);
        else
            throw new DeleteFashionException(null, "");
    }

    /*
     * findRunStart. Status BTreeFile::findRunStart (const void lo_key, RID
     * *pstartrid)
     * 
     * find left-most occurrence of `lo_key', going all the way left if lo_key
     * is null.
     * 
     * Starting record returned in *pstartrid, on page *pppage, which is pinned.
     * 
     * Since we allow duplicates, this must "go left" as described in the text
     * (for the search algorithm).
     * 
     * @param lo_key find left-most occurrence of `lo_key', going all the way
     * left if lo_key is null.
     * 
     * @param startrid it will reurn the first rid =< lo_key
     * 
     * @return return a BTLeafPage instance which is pinned. null if no key was
     * found.
     *
     * ASantra [1/7/2023]: Modified]
     */

    BTLeafPage findRunStart(KeyClass lo_key, RID startrid) throws IOException,
            IteratorException, KeyNotMatchException, ConstructPageException,
            PinPageException, UnpinPageException {
        BTLeafPage pageLeaf;
        BTIndexPage pageIndex;
        Page page;
        BTSortedPage sortPage;
        PageId pageno;
        PageId curpageno = null; // Iterator
        PageId prevpageno;
        PageId nextpageno;
        RID curRid;
        KeyDataEntry curEntry;

        pageno = headerPage.get_rootId();

        if (pageno.pid == INVALID_PAGE) { // no pages in the BTREE
            pageLeaf = null; // should be handled by
            // startrid =INVALID_PAGEID ; // the caller
            return pageLeaf;
        }

        page = pinPage(pageno);
        sortPage = new BTSortedPage(page, headerPage.get_keyType());

        if (trace != null) {
            trace.writeBytes("VISIT node " + pageno + lineSep);
            trace.flush();
        }

        // ASSERTION
        // - pageno and sortPage is the root of the btree
        // - pageno and sortPage valid and pinned

        while (sortPage.getType() == NodeType.INDEX) {
            pageIndex = new BTIndexPage(page, headerPage.get_keyType());
            prevpageno = pageIndex.getPrevPage();
            curEntry = pageIndex.getFirst(startrid);
            while (curEntry != null && lo_key != null
                    && BT.keyCompare(curEntry.key, lo_key) < 0) {

                prevpageno = ((IndexData) curEntry.data).getData();
                curEntry = pageIndex.getNext(startrid);
            }

            unpinPage(pageno);

            pageno = prevpageno;
            page = pinPage(pageno);
            sortPage = new BTSortedPage(page, headerPage.get_keyType());

            if (trace != null) {
                trace.writeBytes("VISIT node " + pageno + lineSep);
                trace.flush();
            }

        }

        pageLeaf = new BTLeafPage(page, headerPage.get_keyType());

        curEntry = pageLeaf.getFirst(startrid);
        while (curEntry == null) {
            // skip empty leaf pages off to left
            nextpageno = pageLeaf.getNextPage();
            unpinPage(pageno);
            if (nextpageno.pid == INVALID_PAGE) {
                // oops, no more records, so set this scan to indicate this.
                return null;
            }

            pageno = nextpageno;
            pageLeaf = new BTLeafPage(pinPage(pageno), headerPage.get_keyType());
            curEntry = pageLeaf.getFirst(startrid);
        }

        // ASSERTIONS:
        // - curkey, curRid: contain the first record on the
        // current leaf page (curkey its key, cur
        // - pageLeaf, pageno valid and pinned

        if (lo_key == null) {
            return pageLeaf;
            // note that pageno/pageLeaf is still pinned;
            // scan will unpin it when done
        }

        while (BT.keyCompare(curEntry.key, lo_key) < 0) {
            curEntry = pageLeaf.getNext(startrid);
            while (curEntry == null) { // have to go right
                nextpageno = pageLeaf.getNextPage();
                unpinPage(pageno);

                if (nextpageno.pid == INVALID_PAGE) {
                    return null;
                }

                pageno = nextpageno;
                pageLeaf = new BTLeafPage(pinPage(pageno),
                        headerPage.get_keyType());

                curEntry = pageLeaf.getFirst(startrid);
            }
        }

        return pageLeaf;
    }

    /*
     * Status BTreeFile::NaiveDelete (const void *key, const RID rid)
     * 
     * Remove specified data entry (<key, rid>) from an index.
     * 
     * We don't do merging or redistribution, but do allow duplicates.
     * 
     * Page containing first occurrence of key `key' is found for us by
     * findRunStart. We then iterate for (just a few) pages, if necesary, to
     * find the one containing <key,rid>, which we then delete via
     * BTLeafPage::delUserRid.
     */

    private boolean NaiveDelete(KeyClass key, RID rid)
            throws LeafDeleteException, KeyNotMatchException, PinPageException,
            ConstructPageException, IOException, UnpinPageException,
            PinPageException, IndexSearchException, IteratorException {

        /*
         * The Delete method simply removes the entry <key, data ptr> from the
         * appropriate BTLeafPage, if it exists. You do not need to implement
         * redistribution or
         * page merging when the number of entries falls below threshold. All duplicate
         * values
         * have to be deleted. This method is given and is set to call NaiveDelete()
         * method by
         * default.
         * 
         * NaiveDelete() method
         * 
         * In NaïveDelete() you need to remove the data entry <key, data ptr> from the
         * leaf page of the index without any merging or redistribution.
         * 
         * The method required to search is already given. It returns the leaf page at
         * the
         * left most part of the tree and then search for the key to be deleted as leaf
         * pages
         * are organized as a doubly link list. You can see the search algorithm in the
         * book
         * for more clarity. You need to be careful if the search key does not exist at
         * the leaf
         * level
         * BTLeafPage findRunStart(key, curRid);
         * 
         * If the search key does not exist at the leaf level, then your code must
         * handle
         * it gracefully by giving proper message on the screen.
         */

        // TODO NaiveDelete() method - https://github.com/lankm/uta-dbms/issues/2
        // [ASantra: 1/14/2024] Remove the return statement and start your code.

        return false;
    }

    /**
     * create a scan with given keys Cases: (1) lo_key = null, hi_key = null
     * scan the whole index (2) lo_key = null, hi_key!= null range scan from min
     * to the hi_key (3) lo_key!= null, hi_key = null range scan from the lo_key
     * to max (4) lo_key!= null, hi_key!= null, lo_key = hi_key exact match (
     * might not unique) (5) lo_key!= null, hi_key!= null, lo_key < hi_key range
     * scan from lo_key to hi_key
     *
     * @param lo_key
     *               the key where we begin scanning. Input parameter.
     * @param hi_key
     *               the key where we stop scanning. Input parameter.
     * @exception IOException
     *                                   error from the lower layer
     * @exception KeyNotMatchException
     *                                   key is not integer key nor string key
     * @exception IteratorException
     *                                   iterator error
     * @exception ConstructPageException
     *                                   error in BT page constructor
     * @exception PinPageException
     *                                   error when pin a page
     * @exception UnpinPageException
     *                                   error when unpin a page
     */
    public BTFileScan new_scan(KeyClass lo_key, KeyClass hi_key)
            throws IOException, KeyNotMatchException, IteratorException,
            ConstructPageException, PinPageException, UnpinPageException

    {
        BTFileScan scan = new BTFileScan();
        if (headerPage.get_rootId().pid == INVALID_PAGE) {
            scan.leafPage = null;
            return scan;
        }

        scan.treeFilename = dbname;
        scan.endkey = hi_key;
        scan.didfirst = false;
        scan.deletedcurrent = false;
        scan.curRid = new RID();
        scan.keyType = headerPage.get_keyType();
        scan.maxKeysize = headerPage.get_maxKeySize();
        scan.bfile = this;

        // this sets up scan at the starting position, ready for iteration
        scan.leafPage = findRunStart(lo_key, scan.curRid);
        return scan;
    }

    void trace_children(PageId id) throws IOException, IteratorException,
            ConstructPageException, PinPageException, UnpinPageException {

        if (trace != null) {

            BTSortedPage sortedPage;
            RID metaRid = new RID();
            PageId childPageId;
            KeyClass key;
            KeyDataEntry entry;
            sortedPage = new BTSortedPage(pinPage(id), headerPage.get_keyType());

            // Now print all the child nodes of the page.
            if (sortedPage.getType() == NodeType.INDEX) {
                BTIndexPage indexPage = new BTIndexPage(sortedPage,
                        headerPage.get_keyType());
                trace.writeBytes("INDEX CHILDREN " + id + " nodes" + lineSep);
                trace.writeBytes(" " + indexPage.getPrevPage());
                for (entry = indexPage.getFirst(metaRid); entry != null; entry = indexPage
                        .getNext(metaRid)) {
                    trace.writeBytes("   " + ((IndexData) entry.data).getData());
                }
            } else if (sortedPage.getType() == NodeType.LEAF) {
                BTLeafPage leafPage = new BTLeafPage(sortedPage,
                        headerPage.get_keyType());
                trace.writeBytes("LEAF CHILDREN " + id + " nodes" + lineSep);
                for (entry = leafPage.getFirst(metaRid); entry != null; entry = leafPage
                        .getNext(metaRid)) {
                    trace.writeBytes("   " + entry.key + " " + entry.data);
                }
            }
            unpinPage(id);
            trace.writeBytes(lineSep);
            trace.flush();
        }

    }

}
